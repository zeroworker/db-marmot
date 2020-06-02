package db.marmot.statistical.generator;

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
		for (StatisticalModel statisticalModel : statisticalModels) {
			if (statisticalModel.isRunning() && !statisticalModel.isCalculated()) {
				Iterator<StatisticalProcedure> procedures = statisticalProcedures.iterator();
				TemporaryMemory temporaryMemory = new StatisticalTemporaryMemory();
				try {
					dataSourceRepository.updateStatisticalModelCalculateIng(statisticalModel);
					DataVolume dataVolume = dataSourceRepository.findDataVolume(statisticalModel.getVolumeCode());
					processedProcedure(procedures, dataVolume,statisticalModel,temporaryMemory);
				} catch (Exception e) {
					log.error("执行模型[%s]数据统计异常", statisticalModel.getModelName(), e);
				} finally {
					try {
						dataSourceRepository.updateStatisticalModelCalculated(statisticalModel, temporaryMemory);
					} catch (Exception e) {
						log.error("更新模型{}计算完成异常", statisticalModel.getModelName(), e);
					}
				}
			}
		}
	}

	@Override
	public void rollBack(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {

	}

	@Override
	public void revise(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask) {

	}

	/**
	 * 执行模型统计
	 * @param iterator
	 * @param dataVolume
	 * @param statisticalModel
	 * @param temporaryMemory
	 */
	private void processedProcedure(Iterator<StatisticalProcedure> iterator,DataVolume dataVolume, StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		if (iterator.hasNext()) {
			StatisticalProcedure procedure = iterator.next();
			if (procedure.match(dataVolume,statisticalModel, temporaryMemory)) {
				procedure.processed(dataVolume,statisticalModel, temporaryMemory);
			}
			processedProcedure(iterator,dataVolume, statisticalModel, temporaryMemory);
		}
	}
}
