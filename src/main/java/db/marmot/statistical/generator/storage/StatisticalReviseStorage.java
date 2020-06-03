package db.marmot.statistical.generator.storage;

import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalReviseTask;
import db.marmot.volume.DataVolume;

import java.util.List;

/**
 * @author shaokang
 */
public class StatisticalReviseStorage extends StatisticalStorage {
	
	private StatisticalReviseTask reviseTask;
	private List<StatisticalModel> statisticalModels;
	
	public StatisticalReviseStorage(DataVolume dataVolume, StatisticalReviseTask reviseTask, List<StatisticalModel> statisticalModels) {
		super(dataVolume);
		Validators.notNull(reviseTask, "订正任务不能为空");
		Validators.notNull(statisticalModels, "统计模型不能为空");
		this.reviseTask = reviseTask;
		this.statisticalModels = statisticalModels;
	}
	
	public boolean hasReviseTask() {
		return reviseTask != null;
	}
	
	public StatisticalReviseTask getReviseTask() {
		return reviseTask;
	}
	
	@Override
	public List<StatisticalModel> getStatisticalModels() {
		return statisticalModels;
	}
}
