package db.marmot.statistical.generator;

import db.marmot.enums.ReviseStatus;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalReviseTask;
import db.marmot.statistical.generator.memory.StatisticalTemporaryMemory;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.statistical.generator.procedure.StatisticalDataCalculateProcedure;
import db.marmot.statistical.generator.procedure.StatisticalDataFetchProcedure;
import db.marmot.statistical.generator.procedure.StatisticalDataMergeProcedure;
import db.marmot.statistical.generator.procedure.StatisticalProcedure;
import db.marmot.volume.DataVolume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataGenerator implements StatisticalGenerator {
	
	private DataSourceRepository dataSourceRepository;
	private List<StatisticalProcedure> statisticalProcedures = new ArrayList<>();
	
	public StatisticalDataGenerator(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
		statisticalProcedures.add(new StatisticalDataFetchProcedure(dataSourceRepository));
		statisticalProcedures.add(new StatisticalDataCalculateProcedure());
		statisticalProcedures.add(new StatisticalDataMergeProcedure(dataSourceRepository));
		statisticalProcedures.sort(Comparator.comparingInt(Ordered::getOrder));
	}
	
	@Override
	public void execute(List<StatisticalModel> statisticalModels) {
		statisticalModels.forEach(this::execute);
	}
	
	private void execute(StatisticalModel statisticalModel) {
		try {
			if (statisticalModel.isRunning() && statisticalModel.isCalculated()) {
				DataVolume dataVolume = dataSourceRepository.findDataVolume(statisticalModel.getVolumeCode());
				dataSourceRepository.updateStatisticalModelCalculateIng(statisticalModel);
				try {
					TemporaryMemory temporaryMemory = new StatisticalTemporaryMemory();
					Iterator<StatisticalProcedure> procedures = statisticalProcedures.iterator();
					processedProcedure(procedures, dataVolume, statisticalModel, temporaryMemory);
				} finally {
					try {
						dataSourceRepository.updateStatisticalModelCalculated(statisticalModel);
					} catch (Exception e) {
						log.error("更新模型{}计算完成异常", statisticalModel.getModelName(), e);
					}
				}
			}
		} catch (Exception e) {
			log.error("执行模型{}数据统计异常", statisticalModel.getModelName(), e);
		}
	}
	
	@Override
	public void rollBack(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {
		try {
			dataSourceRepository.updateStatisticalReviseTaskRollBacking(reviseTask);
			try {
				
				dataSourceRepository.updateStatisticalReviseTaskRolledBack(reviseTask);
			} finally {
				try {
					reviseTask.setReviseStatus(ReviseStatus.non_execute);
					dataSourceRepository.updateStatisticalReviseTask(reviseTask);
				} catch (Exception e) {
					log.error("更新统计订正任务{}为non_execute异常", reviseTask.getVolumeCode(), e);
				}
			}
		} catch (Exception e) {
			log.error("统计订正任务{}-执行回滚异常", reviseTask.getVolumeCode());
		}
	}
	
	@Override
	public void revise(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {
		try {
			dataSourceRepository.updateStatisticalReviseTaskRevising(reviseTask);
			try {
				
				dataSourceRepository.updateStatisticalReviseTaskRevised(reviseTask);
			} finally {
				try {
					reviseTask.setReviseStatus(ReviseStatus.rolled_back);
					dataSourceRepository.updateStatisticalReviseTask(reviseTask);
				} catch (Exception e) {
					log.error("更新统计订正任务{}为rolled_back异常", reviseTask.getVolumeCode(), e);
				}
			}
		} catch (Exception e) {
			log.error("统计订正任务{}-执行订正异常", reviseTask.getVolumeCode());
		}
	}
	
	/**
	 * 执行模型统计
	 * @param iterator
	 * @param dataVolume
	 * @param statisticalModel
	 * @param temporaryMemory
	 */
	private void processedProcedure(Iterator<StatisticalProcedure> iterator, DataVolume dataVolume, StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		if (iterator.hasNext()) {
			StatisticalProcedure procedure = iterator.next();
			if (procedure.match(dataVolume, statisticalModel, temporaryMemory)) {
				procedure.processed(dataVolume, statisticalModel, temporaryMemory);
			}
			processedProcedure(iterator, dataVolume, statisticalModel, temporaryMemory);
		}
	}
}
