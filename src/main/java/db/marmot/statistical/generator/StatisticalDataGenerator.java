package db.marmot.statistical.generator;

import db.marmot.enums.RepositoryType;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalRepository;
import db.marmot.statistical.generator.memory.StatisticalTemporaryMemory;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.statistical.generator.procedure.StatisticalDataCalculateProcedure;
import db.marmot.statistical.generator.procedure.StatisticalDataFetchProcedure;
import db.marmot.statistical.generator.procedure.StatisticalDataMergeProcedure;
import db.marmot.statistical.generator.procedure.StatisticalProcedure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataGenerator implements StatisticalGenerator {
	
	private final ReentrantLock lock = new ReentrantLock();
	private StatisticalRepository statisticalRepository;
	private List<StatisticalProcedure> statisticalProcedures = new ArrayList<>();
	
	public StatisticalDataGenerator(RepositoryAdapter repositoryAdapter) {
		this.statisticalRepository = repositoryAdapter.getRepository(RepositoryType.statistical);
		statisticalProcedures.add(new StatisticalDataFetchProcedure(repositoryAdapter));
		statisticalProcedures.add(new StatisticalDataCalculateProcedure());
		statisticalProcedures.add(new StatisticalDataMergeProcedure(statisticalRepository));
		statisticalProcedures.sort(Comparator.comparingInt(Ordered::getOrder));
	}
	
	@Override
	public void execute(List<StatisticalModel> statisticalModels) {
		for (StatisticalModel statisticalModel : statisticalModels) {
			if (statisticalModel.isRunning() && !statisticalModel.isCalculated()) {
				Iterator<StatisticalProcedure> procedures = statisticalProcedures.iterator();
				TemporaryMemory temporaryMemory = new StatisticalTemporaryMemory();
				try {
					synchronized (statisticalModel) {
						statisticalRepository.updateStatisticalModelCalculateIng(statisticalModel);
					}
					processedProcedure(procedures, statisticalModel, temporaryMemory);
				} catch (Exception e) {
					log.error("执行模型[%s]数据统计异常", statisticalModel.getModelName(), e);
				} finally {
					try {
						statisticalRepository.updateStatisticalModelCalculated(statisticalModel, temporaryMemory);
					} catch (Exception e) {
						log.error("更新模型{}计算完成异常", statisticalModel.getModelName(), e);
					}
				}
			}
		}
	}
	
	/**
	 * 执行模型统计
	 * @param iterator
	 * @param statisticalModel
	 * @param temporaryMemory
	 */
	private void processedProcedure(Iterator<StatisticalProcedure> iterator, StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		if (iterator.hasNext()) {
			StatisticalProcedure procedure = iterator.next();
			if (procedure.match(statisticalModel, temporaryMemory)) {
				procedure.processed(statisticalModel, temporaryMemory);
			}
			processedProcedure(iterator, statisticalModel, temporaryMemory);
		}
	}
}
