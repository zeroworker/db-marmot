package db.marmot.statistical.generator;

import db.marmot.enums.ReviseStatus;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalReviseTask;
import db.marmot.statistical.generator.procedure.*;
import db.marmot.statistical.generator.storage.StatisticalDefaultStorage;
import db.marmot.statistical.generator.storage.StatisticalReviseStorage;
import db.marmot.volume.DataVolume;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataGenerator implements StatisticalGenerator {
	
	private DataSourceRepository dataSourceRepository;

	public StatisticalDataGenerator(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
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
					StatisticalProcedure procedure = new StatisticalDefaultProcedure(
							new StatisticalCalculateProcedure(
									new StatisticalMergeProcedure(
											new StatisticalDefaultStorage(dataVolume, statisticalModel)
											,dataSourceRepository)
							), dataSourceRepository
					);
					if (procedure.match()) {
						procedure.processed();
					}
					dataSourceRepository.updateStatisticalCountStorage(procedure.statisticalStorage());
				} catch (Exception e) {
					log.error("执行模型{}数据统计异常", statisticalModel.getModelName(), e);
					dataSourceRepository.updateStatisticalModelCalculated(statisticalModel);
				}
			}
		} catch (Exception e) {
			log.error("执行模型{}数据统计异常", statisticalModel.getModelName(), e);
		}
	}
	
	@Override
	public void rollBack(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {
		try {
			DataVolume dataVolume = dataSourceRepository.findDataVolume(reviseTask.getVolumeCode());
			dataSourceRepository.updateStatisticalReviseTaskRollBacking(reviseTask);
			try {
				StatisticalProcedure procedure = new StatisticalReviseProcedure(
						new StatisticalCalculateProcedure(
								new StatisticalMergeProcedure(
										new StatisticalReviseStorage(dataVolume, reviseTask, statisticalModels)
										,dataSourceRepository)
						,true
						)
						, dataSourceRepository
				);
				if (procedure.match()) {
					procedure.processed();
				}
				dataSourceRepository.updateRolledBackStatisticalReviseStorage(procedure.statisticalStorage());
			} catch (Exception e) {
				log.error("更新统计订正任务{}为non_execute异常", reviseTask.getVolumeCode(), e);
				reviseTask.setReviseStatus(ReviseStatus.non_execute);
				dataSourceRepository.updateStatisticalReviseTask(reviseTask);
			}
		} catch (Exception e) {
			log.error("统计订正任务{}-执行回滚异常", reviseTask.getVolumeCode());
		}
	}
	
	@Override
	public void revise(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {
		try {
			DataVolume dataVolume = dataSourceRepository.findDataVolume(reviseTask.getVolumeCode());
			dataSourceRepository.updateStatisticalReviseTaskRevising(reviseTask);
			try {
				StatisticalProcedure procedure = new StatisticalReviseProcedure(
						new StatisticalCalculateProcedure(
								new StatisticalMergeProcedure(
										new StatisticalReviseStorage(dataVolume, reviseTask, statisticalModels)
										,dataSourceRepository)
								,false
						)
						, dataSourceRepository
				);
				if (procedure.match()) {
					procedure.processed();
				}
				dataSourceRepository.updateRevisedStatisticalReviseStorage(procedure.statisticalStorage());
			} catch (Exception e) {
				log.error("更新统计订正任务{}为rolled_back异常", reviseTask.getVolumeCode(), e);
				reviseTask.setReviseStatus(ReviseStatus.rolled_back);
				dataSourceRepository.updateStatisticalReviseTask(reviseTask);
			}
		} catch (Exception e) {
			log.error("统计订正任务{}-执行订正异常", reviseTask.getVolumeCode());
		}
	}
}
