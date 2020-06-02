package db.marmot.statistical;

import com.google.common.base.Splitter;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.GraphicCycle;
import db.marmot.enums.WindowType;
import db.marmot.enums.WindowUnit;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.converter.GraphicCycleConverter;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public class StatisticalModelBuilder {
	
	private DataVolume dataVolume;
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
		statisticalModel.setOffsetExpr(Splitter.on(",").splitToList(offsetExpr));
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
		this.dataVolume = dataVolume;
		statisticalModel.setVolumeCode(dataVolume.getVolumeCode());
		return this;
	}
	
	public StatisticalModelBuilder addGraphic(Graphic graphic) {
		statisticalModel.setWindowType(WindowType.sliding_time);
		statisticalModel.setModelName(graphic.getGraphicModel().getModelName());
		statisticalModel.setOffsetExpr(Splitter.on(",").splitToList(graphic.getGraphicModel().getOffsetExpr()));
		if (graphic.getGraphicCycle() != GraphicCycle.non) {
			GraphicCycleConverter graphicCycleConverter = ConverterAdapter.getInstance().getGraphicCycleConverter(graphic.getGraphicCycle());
			statisticalModel.setWindowUnit(graphicCycleConverter.windowUnit());
			return this;
		}
		statisticalModel.setWindowUnit(WindowUnit.non);
		statisticalModel.setDirectionColumns(graphic.getGraphicModel().getDirectionColumns());
		return this;
	}
	
	public StatisticalModel builder() {
		statisticalModel.validateStatisticalModel(dataVolume);
		return statisticalModel;
	}
}
