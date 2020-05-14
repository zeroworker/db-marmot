package db.marmot.statistical.generator.procedure;

import java.util.Date;
import java.util.Map;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.OperatorsConverter;
import db.marmot.enums.WindowUnit;
import db.marmot.statistical.*;
import db.marmot.statistical.generator.convert.WindowUnitConverter;
import db.marmot.statistical.generator.memory.TemporaryMemory;

/**
 * @author shaokang
 */
public class StatisticalDataCalculateProcedure implements StatisticalProcedure {
	
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	@Override
	public boolean match(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		return temporaryMemory.hashMetaData();
	}
	
	@Override
	public void processed(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		for (Map<String, Object> data : temporaryMemory.getMetaData()) {
			if (statisticalFilter(statisticalModel, data)) {
				boolean direction = statisticalDirection(statisticalModel, data);
				for (String expr : statisticalModel.getOffsetExpr()) {
					int offset = converterAdapter.eval(expr);
					aggregateCalculate(statisticalModel, temporaryMemory, data, offset, direction);
				}
			}
		}
	}
	
	private boolean statisticalFilter(StatisticalModel statisticalModel, Map<String, Object> data) {
		
		Object timeValue = data.get(statisticalModel.getTimeColumn());
		if (statisticalModel.getWindowUnit() != WindowUnit.NON) {
			if (timeValue == null || !(timeValue instanceof Date)) {
				return false;
			}
		}
		
		for (ConditionColumn conditionColumn : statisticalModel.getConditionColumns()) {
			Object leftValue = data.get(conditionColumn.getColumnCode());
			OperatorsConverter operatorsConverter = converterAdapter.getOperatorsConverter(conditionColumn.getOperators());
			if (!operatorsConverter.compareValue(conditionColumn.getColumnType(), leftValue, conditionColumn.getRightValue())) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean statisticalDirection(StatisticalModel statisticalModel, Map<String, Object> data) {
		
		for (DirectionColumn directionColumn : statisticalModel.getDirectionColumns()) {
			Object leftValue = data.get(directionColumn.getColumnCode());
			OperatorsConverter operatorsConverter = converterAdapter.getOperatorsConverter(directionColumn.getOperators());
			if (!operatorsConverter.compareValue(directionColumn.getColumnType(), leftValue, directionColumn.getRightValue())) {
				return false;
			}
		}
		return true;
	}
	
	private void aggregateCalculate(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory, Map<String, Object> data, int offset, boolean direction) {
		
		Date timeValue = (Date) data.get(statisticalModel.getTimeColumn());
		String rowKey = statisticalModel.createRowKey(data, timeValue, offset);
		
		if (!temporaryMemory.hashStatisticalData(rowKey)) {
			StatisticalData statisticalData = createStatisticalData(statisticalModel, data, rowKey, offset);
			temporaryMemory.addStatisticalData(statisticalData);
		}
		
		for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
			Object rightValue = data.get(column.getColumnCode());
			converterAdapter.getAggregatesConverter(column.getAggregates()).calculate(temporaryMemory, rowKey, column.getColumnCode(), rightValue, direction);
		}
	}
	
	private StatisticalData createStatisticalData(StatisticalModel statisticalModel, Map<String, Object> data, String rowKey, int offset) {
		
		StatisticalData statisticalData = new StatisticalData(statisticalModel.getModelName(), rowKey);
		
		for (GroupColumn column : statisticalModel.getGroupColumns()) {
			Object groupValue = data.get(column.getColumnCode());
			if (groupValue != null) {
				statisticalData.getGroupColumns().add(groupValue);
			}
		}
		
		if (statisticalModel.getWindowUnit() != WindowUnit.NON) {
			Date timeValue = (Date) data.get(statisticalModel.getTimeColumn());
			WindowUnitConverter windowUnitConverter = converterAdapter.getWindowUnitConverter(statisticalModel.getWindowUnit());
			statisticalData.setTimeUnit(windowUnitConverter.getTimeUnit(timeValue, offset));
		}
		
		return statisticalData;
	}
	
	@Override
	public int getOrder() {
		return 2;
	}
}
