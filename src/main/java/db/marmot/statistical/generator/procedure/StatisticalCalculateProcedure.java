package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.OperatorsConverter;
import db.marmot.enums.WindowUnit;
import db.marmot.statistical.*;
import db.marmot.statistical.generator.convert.WindowUnitConverter;
import db.marmot.statistical.generator.storage.StatisticalStorage;
import db.marmot.volume.DataVolume;
import org.mvel2.MVEL;

import java.util.Date;
import java.util.Map;

/**
 * @author shaokang
 */
public class StatisticalCalculateProcedure extends StatisticalProcedureWrapper {
	
	private boolean rollBack = false;

	public StatisticalCalculateProcedure(StatisticalProcedure statisticalProcedure) {
		super(statisticalProcedure);
	}

	public StatisticalCalculateProcedure(StatisticalProcedure statisticalProcedure, boolean rollBack) {
		super(statisticalProcedure);
		this.rollBack = rollBack;
	}
	
	@Override
	public boolean match() {
		return statisticalStorage().hashMetaData();
	}
	
	@Override
	public void processed() {
		DataVolume dataVolume = statisticalStorage().getDataVolume();
		statisticalStorage().getStatisticalModels().forEach(statisticalModel -> {
			for (Map<String, Object> data : statisticalStorage().getMetaData()) {
				if (statisticalFilter(dataVolume, statisticalModel, data)) {
					boolean direction = statisticalDirection(statisticalModel, data);
					for (String expr : statisticalModel.getOffsetExpr()) {
						int offset = MVEL.eval(expr, Integer.class);
						aggregateCalculate(dataVolume, statisticalModel, statisticalStorage(), data, offset, direction);
					}
				}
			}
		});
		super.processed();
	}
	
	private boolean statisticalFilter(DataVolume dataVolume, StatisticalModel statisticalModel, Map<String, Object> data) {
		Object timeValue = data.get(dataVolume.findDateDataColumn().getColumnCode());
		if (statisticalModel.getWindowUnit() != WindowUnit.non) {
			if (timeValue == null || !(timeValue instanceof Date)) {
				return false;
			}
		}
		for (ConditionColumn conditionColumn : statisticalModel.getConditionColumns()) {
			Object leftValue = data.get(conditionColumn.getColumnCode());
			OperatorsConverter operatorsConverter = ConverterAdapter.getInstance().getOperatorsConverter(conditionColumn.getOperators());
			if (!operatorsConverter.compareValue(conditionColumn.getColumnType(), leftValue, conditionColumn.getRightValue())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean statisticalDirection(StatisticalModel statisticalModel, Map<String, Object> data) {
		for (DirectionColumn directionColumn : statisticalModel.getDirectionColumns()) {
			Object leftValue = data.get(directionColumn.getColumnCode());
			OperatorsConverter operatorsConverter = ConverterAdapter.getInstance().getOperatorsConverter(directionColumn.getOperators());
			if (!operatorsConverter.compareValue(directionColumn.getColumnType(), leftValue, directionColumn.getRightValue())) {
				return rollBack ? true : false;
			}
		}
		return rollBack ? false : true;
	}
	
	private void aggregateCalculate(DataVolume dataVolume, StatisticalModel statisticalModel, StatisticalStorage statisticalStorage, Map<String, Object> data, int offset, boolean direction) {
		Date timeValue = (Date) data.get(dataVolume.findDateDataColumn().getColumnCode());
		String rowKey = statisticalModel.createRowKey(data, timeValue, offset);
		if (!statisticalStorage.hashStatisticalData(rowKey)) {
			StatisticalData statisticalData = createStatisticalData(dataVolume, statisticalModel, data, rowKey, offset);
			statisticalStorage.addStatisticalData(statisticalData);
		}
		for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
			Object rightValue = data.get(column.getColumnCode());
			ConverterAdapter.getInstance().getAggregatesConverter(column.getAggregates()).calculate(statisticalStorage, rowKey, column.getColumnCode(), rightValue, direction);
		}
	}
	
	private StatisticalData createStatisticalData(DataVolume dataVolume, StatisticalModel statisticalModel, Map<String, Object> data, String rowKey, int offset) {
		StatisticalData statisticalData = new StatisticalData(statisticalModel.getModelName(), rowKey);
		for (GroupColumn column : statisticalModel.getGroupColumns()) {
			Object groupValue = data.get(column.getColumnCode());
			if (groupValue != null) {
				statisticalData.getGroupColumns().add(groupValue);
			}
		}
		if (statisticalModel.getWindowUnit() != WindowUnit.non) {
			Date timeValue = (Date) data.get(dataVolume.findDateDataColumn().getColumnCode());
			WindowUnitConverter windowUnitConverter = ConverterAdapter.getInstance().getWindowUnitConverter(statisticalModel.getWindowUnit());
			statisticalData.setTimeUnit(windowUnitConverter.getTimeUnit(timeValue, offset));
		}
		return statisticalData;
	}
}
