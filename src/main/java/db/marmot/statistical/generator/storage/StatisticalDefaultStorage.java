package db.marmot.statistical.generator.storage;

import com.google.common.collect.Lists;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalException;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTask;
import db.marmot.volume.DataRange;
import db.marmot.volume.DataVolume;

import java.util.List;

/**
 * @author shaokang
 */
public class StatisticalDefaultStorage extends StatisticalStorage {
	
	private StatisticalTask thisTask;
	private StatisticalTask nextTask;
	private StatisticalModel statisticalModel;
	
	public StatisticalDefaultStorage(DataVolume dataVolume, StatisticalModel statisticalModel) {
		super(dataVolume);
		Validators.notNull(statisticalModel, "统计模型不能为空");
		this.statisticalModel = statisticalModel;
	}
	
	public void addThisTask(StatisticalTask statisticalTask) {
		if (this.thisTask != null) {
			throw new StatisticalException("本次统计任务已经存在");
		}
		this.thisTask = statisticalTask;
	}
	
	public void addNextTask(DataRange dataRange, DataVolume dataVolume, StatisticalModel statisticalModel) {
		if (this.nextTask != null) {
			throw new StatisticalException("下次统计任务已经存在");
		}
		StatisticalTask statisticalTask = new StatisticalTask();
		statisticalTask.setScanned(false);
		statisticalTask.setStartIndex(dataRange.getMinValue());
		statisticalTask.setModelName(statisticalModel.getModelName());
		statisticalTask.setEndIndex(dataRange.calculateEndIndex(dataVolume.getVolumeLimit()));
		this.nextTask = statisticalTask;
	}
	
	public boolean hashThisTask() {
		return this.thisTask != null && !this.thisTask.isScanned();
	}
	
	public StatisticalTask getThisTask() {
		return this.thisTask;
	}
	
	public boolean hashNextTask() {
		return this.nextTask != null;
	}
	
	public StatisticalTask getNextTask() {
		return this.nextTask;
	}
	
	public StatisticalModel getStatisticalModel() {
		return statisticalModel;
	}
	
	@Override
	public List<StatisticalModel> getStatisticalModels() {
		return Lists.newArrayList(statisticalModel);
	}
}
