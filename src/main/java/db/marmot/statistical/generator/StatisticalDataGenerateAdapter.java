package db.marmot.statistical.generator;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.ReviseStatus;
import db.marmot.enums.WindowUnit;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataGenerateAdapter implements StatisticalGenerateAdapter, ApplicationContextAware, InitializingBean, ApplicationListener<ContextClosedEvent> {
	
	private int maxPoolSize;
	private int reviseDelay;
	private DataSourceRepository dataSourceRepository;
	private ApplicationContext applicationContext;
	private StatisticalGenerator statisticalGenerator;
	private ThreadPoolTaskExecutor statisticalThreadPool;
	private final ReentrantLock reviseLock = new ReentrantLock();
	private final ReentrantLock generateLock = new ReentrantLock();
	private final ReentrantLock rollbackLock = new ReentrantLock();
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public StatisticalDataGenerateAdapter(int maxPoolSize, int reviseDelay, DataSourceRepository dataSourceRepository) {
		this.maxPoolSize = maxPoolSize;
		this.reviseDelay = reviseDelay;
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	public void generateStatisticalData() {
		generateLock.lock();
		try {
			List<StatisticalModel> statisticalModels = dataSourceRepository.findNormalStatisticalModels();
			if (statisticalModels != null && !statisticalModels.isEmpty()) {
				Iterator<StatisticalModel> iterator = statisticalModels.iterator();
				List<Integer> threadsModelNum = calculateThreadModelNum(statisticalModels.size());
				for (Integer modelNum : threadsModelNum) {
					try {
						List<StatisticalModel> threadModels = createThreadModels(modelNum, iterator);
						statisticalThreadPool.execute(() -> statisticalGenerator.execute(threadModels));
					} catch (TaskRejectedException taskRejectedException) {
						log.warn("异步执行模型统计数据生成任务提交拒绝 当前无可用线程");
					}
				}
			}
		} catch (Exception e) {
			log.error("统计数据生成异常", e);
		}
		generateLock.unlock();
	}
	
	@Override
	public void reviseStatisticalModel() {
		reviseLock.lock();
		try {
			List<StatisticalModel> statisticalModels = dataSourceRepository.findReviseStatisticalModels(reviseDelay);
			if (CollectionUtils.isNotEmpty(statisticalModels)) {
				statisticalModels.forEach(statisticalModel -> dataSourceRepository.updateStatisticalModelCalculated(statisticalModel));
			}
		} catch (Exception e) {
			log.error("订正统计模型计算状态异常", e);
		}
		reviseLock.unlock();
	}
	
	@Override
	public void rollbackStatisticalData() {
		rollbackLock.lock();
		try {
			List<StatisticalReviseTask> statisticalReviseTasks = dataSourceRepository.queryPageStatisticalReviseTasks(null, ReviseStatus.non_execute, 0, 1);
			if (CollectionUtils.isNotEmpty(statisticalReviseTasks)) {
				StatisticalReviseTask statisticalReviseTask = statisticalReviseTasks.stream().findFirst().get();
				List<StatisticalModel> statisticalModels = dataSourceRepository.findStatisticalModels(statisticalReviseTask.getVolumeCode());
				if (CollectionUtils.isNotEmpty(statisticalModels)) {
					try {
						statisticalThreadPool.execute(() -> statisticalGenerator.rollBack(statisticalModels, statisticalReviseTask));
					} catch (TaskRejectedException taskRejectedException) {
						log.warn("当前任务繁忙,无可用线程执行统计回滚任务");
					}
				}
			}
		} catch (Exception e) {
			log.error("统计订正任务-回滚异常", e);
		}
		rollbackLock.unlock();
	}
	
	@Override
	public void reviseStatisticalData(long taskId) {
		StatisticalReviseTask statisticalReviseTask = dataSourceRepository.findStatisticalReviseTask(taskId);
		Validators.isTrue(statisticalReviseTask.getReviseStatus() == ReviseStatus.rolled_back, "统计订正任务未回滚完成");
		List<StatisticalModel> statisticalModels = dataSourceRepository.findStatisticalModels(statisticalReviseTask.getVolumeCode());
		if (CollectionUtils.isNotEmpty(statisticalModels)) {
			try {
				statisticalThreadPool.execute(() -> statisticalGenerator.revise(statisticalModels, statisticalReviseTask));
			} catch (TaskRejectedException taskRejectedException) {
				throw new StatisticalException("当前任务繁忙,无可用线程执行统计订正任务");
			}
		}
	}
	
	@Override
	public Map<String, Object> getAggregateData(String modelName, Map<String, Object> groupData) {
		StatisticalModel statisticalModel = dataSourceRepository.findStatisticalModel(modelName);
		Validators.isTrue(statisticalModel.getWindowUnit() == WindowUnit.non, "模型%s非粒度模型", modelName);
		String rowKey = statisticalModel.createRowKey(groupData, null, 0);
		StatisticalData statisticalData = dataSourceRepository.findStatisticalData(statisticalModel.getModelName(), rowKey);
		return getAggregateData(statisticalModel, statisticalData);
	}
	
	@Override
	public Map<String, Object> getAggregateData(String modelName, Date timeValue, Map<String, Object> groupData) {
		return getAggregateData(modelName, 0, timeValue, groupData);
	}
	
	@Override
	public Map<String, Object> getAggregateData(String modelName, int offset, Date timeValue, Map<String, Object> groupData) {
		StatisticalModel statisticalModel = dataSourceRepository.findStatisticalModel(modelName);
		String rowKey = statisticalModel.createRowKey(groupData, timeValue, 0);
		StatisticalData statisticalData = dataSourceRepository.findStatisticalData(statisticalModel.getModelName(), rowKey);
		return getAggregateData(statisticalModel, statisticalData);
	}
	
	@Override
	public List<Map<String, Object>> getAggregateData(String modelName, int offset, Map<Date, Map<String, Object>> timeGroupData) {
		StatisticalModel statisticalModel = dataSourceRepository.findStatisticalModel(modelName);
		
		List<String> rowKeys = new ArrayList<>();
		timeGroupData.forEach((timeValue, groupData) -> rowKeys.add(statisticalModel.createRowKey(groupData, timeValue, offset)));
		
		List<Map<String, Object>> aggregateData = new ArrayList<>();
		List<StatisticalData> statisticalData = dataSourceRepository.findStatisticalData(modelName, rowKeys);
		
		for (String rowKey : rowKeys) {
			int index = statisticalData.indexOf(new StatisticalData(modelName, rowKey));
			aggregateData.add(getAggregateData(statisticalModel, index >= 0 ? statisticalData.get(index) : null));
		}
		
		return aggregateData;
	}
	
	@Override
	public Map<String, Object> getAggregateData(String modelName, Date startTime, Date endTime, Map<String, Object> groupData) {
		return getAggregateData(modelName, 0, startTime, endTime, groupData);
	}
	
	@Override
	public Map<String, Object> getAggregateData(List<String> modelNames, int offset, Date startTime, Date endTime, Map<String, Object> groupData) {
		Map<String, Object> aggregateData = new HashMap<>();
		for (String modelName : modelNames) {
			aggregateData.putAll(getAggregateData(modelName, 0, startTime, endTime, groupData));
		}
		return aggregateData;
	}
	
	@Override
	public Map<String, Object> getAggregateData(String modelName, int offset, Date startTime, Date endTime, Map<String, Object> groupData) {
		StatisticalModel statisticalModel = dataSourceRepository.findStatisticalModel(modelName);
		
		LocalDateTime startLocalTime = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime endLocalTime = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		List<String> rowKeys = new ArrayList<>();
		while (!startLocalTime.isAfter(endLocalTime)) {
			rowKeys.add(statisticalModel.createRowKey(groupData, Date.from(startLocalTime.atZone(ZoneId.systemDefault()).toInstant()), offset));
			startLocalTime.plusDays(1);
		}
		
		List<StatisticalData> statisticalData = dataSourceRepository.findStatisticalData(modelName, rowKeys);
		
		return getAggregateData(statisticalModel, statisticalData);
	}
	
	private Map<String, Object> getAggregateData(StatisticalModel statisticalModel, StatisticalData statisticalData) {
		Map<String, Object> aggregateData = new HashMap<>();
		if (statisticalData != null) {
			aggregateData.put("timeUnit", statisticalData.getTimeUnit());
			for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
				Object aggregateValue = converterAdapter.getAggregatesConverter(column.getAggregates()).getAggregateValue(column.getColumnCode(), statisticalData);
				aggregateData.put(column.getColumnCode(), aggregateValue);
			}
		}
		return aggregateData;
	}
	
	private Map<String, Object> getAggregateData(StatisticalModel statisticalModel, List<StatisticalData> statisticalData) {
		Map<String, Object> aggregateData = new HashMap<>();
		if (statisticalData != null) {
			for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
				Object aggregateValue = converterAdapter.getAggregatesConverter(column.getAggregates()).getAggregateValue(column.getColumnCode(), statisticalData);
				aggregateData.put(column.getColumnCode(), aggregateValue);
			}
		}
		return aggregateData;
	}
	
	@Override
	public void afterPropertiesSet() {
		Validators.isTrue(maxPoolSize > 1, "maxPoolSize 必须大于1");
		
		this.statisticalGenerator = new StatisticalDataGenerator(dataSourceRepository);
		
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		RootBeanDefinition db = new RootBeanDefinition(ThreadPoolTaskExecutor.class);
		db.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues threadPoolPropertyValues = new MutablePropertyValues();
		threadPoolPropertyValues.add("corePoolSize", 1);
		threadPoolPropertyValues.add("maxPoolSize", maxPoolSize);
		threadPoolPropertyValues.add("queueCapacity", 0);
		threadPoolPropertyValues.add("keepAliveSeconds", 20);
		threadPoolPropertyValues.add("threadNamePrefix", "marmot-statistical");
		threadPoolPropertyValues.add("rejectedExecutionHandler", new ThreadPoolExecutor.DiscardPolicy());
		db.setPropertyValues(threadPoolPropertyValues);
		factory.registerBeanDefinition("marmot.statistical.threadPool", db);
		this.statisticalThreadPool = factory.getBean("marmot.statistical.threadPool", ThreadPoolTaskExecutor.class);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		statisticalThreadPool.shutdown();
	}
	
	/**
	 * 计算线程模型数
	 * @param modelNum 模型数
	 * @return
	 */
	private List<Integer> calculateThreadModelNum(int modelNum) {
		int threadNum = maxPoolSize = statisticalThreadPool.getActiveCount();
		List<Integer> threadsModelNum = new ArrayList<>();
		for (int i = 0; i < threadNum; i++) {
			int seqNo = i;
			int max = modelNum * (seqNo + 1) / threadNum;
			int min = modelNum * seqNo / threadNum;
			int threadModelNum = max - min;
			if (threadModelNum > 0) {
				threadsModelNum.add(threadModelNum);
			}
		}
		return threadsModelNum;
	}
	
	/**
	 * 创建线程执行模型数
	 * @param modelNum
	 * @param iterator
	 * @return
	 */
	private List<StatisticalModel> createThreadModels(Integer modelNum, Iterator<StatisticalModel> iterator) {
		List<StatisticalModel> threadModels = new ArrayList<>();
		for (int i = 0; i < modelNum; i++) {
			if (iterator.hasNext()) {
				threadModels.add(iterator.next());
			}
		}
		return threadModels;
	}
}
