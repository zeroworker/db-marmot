package db.marmot.statistical;

import com.google.common.collect.Lists;
import db.marmot.enums.WindowType;
import db.marmot.enums.WindowUnit;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public class StatisticalModelBuilder {
	
	private StatisticalModel statisticalModel = new StatisticalModel();
	
	public StatisticalModelBuilder addMemo(String memo) {
		statisticalModel.setMemo(memo);
		return this;
	}
	
	public StatisticalModelBuilder addModelName(String modelName) {
		statisticalModel.setModelName(modelName);
		return this;
	}
	
	public StatisticalModelBuilder addWindowLength(int windowLength) {
		statisticalModel.setWindowLength(windowLength);
		return this;
	}
	
	public StatisticalModelBuilder addWindowType(WindowType windowType) {
		statisticalModel.setWindowType(windowType);
		return this;
	}
	
	public StatisticalModelBuilder addWindowUnit(WindowUnit windowUnit) {
		statisticalModel.setWindowUnit(windowUnit);
		return this;
	}
	
	public StatisticalModelBuilder addOffsetExpr(String offsetExpr) {
		statisticalModel.getOffsetExpr().addAll(Lists.newArrayList(offsetExpr.split(",")));
		return this;
	}
	
	public StatisticalModelBuilder addAggregateColumn(AggregateColumn aggregateColumn) {
		statisticalModel.getAggregateColumns().add(aggregateColumn);
		return this;
	}
	
	public StatisticalModelBuilder addConditionColumn(ConditionColumn conditionColumn) {
		statisticalModel.getConditionColumns().add(conditionColumn);
		return this;
	}
	
	public StatisticalModelBuilder addGroupColumn(GroupColumn groupColumn) {
		statisticalModel.getGroupColumns().add(groupColumn);
		return this;
	}
	
	public StatisticalModelBuilder addDirectionColumn(DirectionColumn directionColumn) {
		statisticalModel.getDirectionColumns().add(directionColumn);
		return this;
	}
	
	public StatisticalModelBuilder addDataVolume(DataVolume dataVolume) {
		statisticalModel.setDbName(dataVolume.getDbName());
		statisticalModel.setFetchSql(dataVolume.getSqlScript());
		statisticalModel.setVolumeCode(dataVolume.getVolumeCode());
		statisticalModel.setFetchStep(dataVolume.getVolumeLimit());
		statisticalModel.setTimeColumn(dataVolume.findDateDataColumn().getColumnCode());
		statisticalModel.setIndexColumn(dataVolume.findIndexColumn().getColumnCode());
		return this;
	}
	
	public StatisticalModel builder() {
		return statisticalModel;
	}
}
