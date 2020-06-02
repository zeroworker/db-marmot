package db.marmot.statistical;

import db.marmot.enums.ReviseStatus;
import db.marmot.graphic.GraphicRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.DataVolume;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

/**
 * @author shaokang
 */
public class StatisticalRepository extends GraphicRepository {
	
	public StatisticalRepository(DataSourceTemplate dataSourceTemplate) {
		super(dataSourceTemplate);
	}
	
	/**
	 * 保存统计模型
	 */
	public void storeStatisticalModel(StatisticalModel statisticalModel) {
		Validators.notNull(statisticalModel, "统计模型不能为空");
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(statisticalModel.getVolumeCode());
		Validators.notNull(dataVolume, "数据集%不存在", dataVolume.getVolumeCode());
		statisticalModel.validateStatisticalModel(dataVolume);
		try {
			dataSourceTemplate.storeStatisticalModel(statisticalModel);
		} catch (DuplicateKeyException e) {
			throw new RepositoryException(String.format("重复统计模型%s", statisticalModel.getModelName()));
		}
	}
	
	/**
	 * 获取模型
	 * @param modelName
	 * @return
	 */
	public StatisticalModel findStatisticalModel(String modelName) {
		StatisticalModel statisticalModel = dataSourceTemplate.findStatisticalModel(modelName);
		Validators.notNull(statisticalModel, "统计模型%s不存在", modelName);
		return statisticalModel;
	}
	
	/**
	 * 获取模型
	 * @param volumeCode
	 * @return
	 */
	public List<StatisticalModel> findStatisticalModels(String volumeCode) {
		List<StatisticalModel> statisticalModels = dataSourceTemplate.findStatisticalModels(volumeCode);
		Validators.isTrue(CollectionUtils.isNotEmpty(statisticalModels), "数据集%s未配置模型", volumeCode);
		return statisticalModels;
	}
	
	/**
	 * 获取可做统计计算的模型
	 * @return
	 */
	public List<StatisticalModel> findNormalStatisticalModels() {
		return dataSourceTemplate.findStatisticalModelByStatus(true, false);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculateIng(StatisticalModel statisticalModel) {
		StatisticalModel originalStatisticalModel = dataSourceTemplate.loadStatisticalModel(statisticalModel.getModelId(), true);
		Validators.notNull(originalStatisticalModel, "统计模型%s不存在或者未计算完成", statisticalModel.getModelName());
		statisticalModel.setCalculated(false);
		dataSourceTemplate.updateStatisticalModel(statisticalModel);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculated(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		StatisticalModel originalStatisticalModel = dataSourceTemplate.loadStatisticalModel(statisticalModel.getModelId(), false);
		Validators.notNull(originalStatisticalModel, "未获取到计算中模型%s", statisticalModel.getModelName());
		if (!originalStatisticalModel.isCalculated()) {
			statisticalModel.setCalculated(true);
			dataSourceTemplate.updateStatisticalModel(statisticalModel);
		}
		if (temporaryMemory.hashMemoryStatistics()) {
			temporaryMemory.getMemoryStatistics().values().forEach(data -> {
				if (dataSourceTemplate.findStatisticalData(data.getModelName(), data.getRowKey()) == null) {
					dataSourceTemplate.storeStatisticalData(data);
					return;
				}
				dataSourceTemplate.updateStatisticalData(data);
			});
		}
		if (temporaryMemory.hashMemoryDistinct()) {
			temporaryMemory.getMemoryDistinct().values().forEach(distinct -> {
				if (dataSourceTemplate.findStatisticalDistinct(distinct.getRowKey(), distinct.getDistinctColumn()) == null) {
					dataSourceTemplate.storeStatisticalDistinct(distinct);
					return;
				}
				dataSourceTemplate.updateStatisticalDistinct(distinct);
			});
		}
		if (temporaryMemory.hashThisTask()) {
			StatisticalTask statisticalTask = dataSourceTemplate.findStatisticalTask(temporaryMemory.getThisTask().getModelName());
			if (temporaryMemory.hashNextTask()) {
				dataSourceTemplate.deleteStatisticalTask(statisticalTask.getTaskId());
			} else {
				statisticalTask.setScanned(true);
				dataSourceTemplate.updateStatisticalTask(statisticalTask);
			}
		}
		if (temporaryMemory.hashNextTask()) {
			dataSourceTemplate.storeStatisticalTask(temporaryMemory.getNextTask());
		}
	}
	
	/**
	 * 获取统计数据
	 * @param modelName
	 * @param rowKey
	 * @return
	 */
	public StatisticalData findStatisticalData(String modelName, String rowKey) {
		return dataSourceTemplate.findStatisticalData(modelName, rowKey);
	}
	
	/**
	 *
	 * @param modelName
	 * @param rowKeys
	 * @return
	 */
	public List<StatisticalData> findStatisticalData(String modelName, List<String> rowKeys) {
		return dataSourceTemplate.findStatisticalData(modelName, rowKeys);
	}
	
	/**
	 * 获取统计任务
	 * @param modelName
	 * @return
	 */
	public StatisticalTask findStatisticalTask(String modelName) {
		return dataSourceTemplate.findStatisticalTask(modelName);
	}
	
	/**
	 * 获取去重数据
	 * @param rowKey
	 * @param distinctColumn
	 * @return
	 */
	public StatisticalDistinct findStatisticalDistinct(String rowKey, String distinctColumn) {
		return dataSourceTemplate.findStatisticalDistinct(rowKey, distinctColumn);
	}
	
	/**
	 * 存储统计订正任务
	 * @param statisticalReviseTask
	 * @return
	 */
	public StatisticalReviseTask storeStatisticalReviseTask(StatisticalReviseTask statisticalReviseTask) {
		Validators.notNull(statisticalReviseTask, "统计订正任务不为空");
		statisticalReviseTask.validateStatisticalReviseTask();
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(statisticalReviseTask.getVolumeCode());
		Validators.notNull(dataVolume, "数据集%s不存在", statisticalReviseTask.getVolumeCode());
		try {
			dataSourceTemplate.storeStatisticalReviseTask(statisticalReviseTask);
		} catch (DuplicateKeyException e) {
			StatisticalReviseTask reviseTask = dataSourceTemplate.loadStatisticalReviseTask(statisticalReviseTask.getVolumeCode());
			Validators.notNull(reviseTask, "数据集%s对应的统计订正任务不存在", reviseTask);
			Validators.isTrue(reviseTask.getReviseStatus() == ReviseStatus.revised, "存在数据集%s未完成的统计订正任务", statisticalReviseTask.getVolumeCode());
			dataSourceTemplate.deleteStatisticalReviseTask(reviseTask.getTaskId());
			dataSourceTemplate.storeStatisticalReviseTask(statisticalReviseTask);
		}
		return statisticalReviseTask;
	}

	public StatisticalReviseTask findStatisticalReviseTask(long taskId){
		StatisticalReviseTask statisticalReviseTask = dataSourceTemplate.findStatisticalReviseTask(taskId);
		Validators.notNull(statisticalReviseTask,"统计订正任务不存在");
		return statisticalReviseTask;
	}

	/**
	 * 分页查询统计订正任务
	 * @param volumeCode
	 * @param reviseStatus
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<StatisticalReviseTask> queryPageStatisticalReviseTasks(String volumeCode, ReviseStatus reviseStatus, int pageNum, int pageSize) {
		return dataSourceTemplate.queryPageStatisticalReviseTasks(volumeCode, reviseStatus == null ? null : reviseStatus.getCode(), pageNum, pageSize);
	}
}
