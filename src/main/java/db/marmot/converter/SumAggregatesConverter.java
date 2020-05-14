package db.marmot.converter;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.generator.memory.TemporaryMemory;

/**
 * @author shaokang
 */
public class SumAggregatesConverter implements AggregatesConverter {
	@Override
	public Aggregates aggregates() {
		return Aggregates.sum;
	}
	
	@Override
	public void validateColumnType(ColumnType columnType) {
		if (columnType != ColumnType.number) {
			throw new ConverterException("聚合函数求和只支持数字类型字段");
		}
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("sum(" + columnCode + ")"), columnCode);
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String expr, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("sum(" + expr + ")"), columnCode);
	}
	
	@Override
	public void calculate(TemporaryMemory temporaryMemory, String rowKey, String columnCode, Object rightValue, boolean direction) {
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		StatisticalData statisticalData = temporaryMemory.getStatisticalData(rowKey);
		BigDecimal sumLeftValue = statisticalData.putIfPresent(aggregateKey, new BigDecimal(0));
		BigDecimal sumRightValue = direction ? (BigDecimal) rightValue : ((BigDecimal) rightValue).negate();
		statisticalData.addAggregateData(aggregateKey, sumLeftValue.add(sumRightValue));
	}
	
	@Override
	public void calculate(TemporaryMemory temporaryMemory, String rowKey, String columnCode, StatisticalData data) {
		StatisticalData memoryData = temporaryMemory.getStatisticalData(rowKey);
		
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		BigDecimal sumRightValue = data.getIfPresent(aggregateKey, new BigDecimal(0));
		BigDecimal sumLeftValue = memoryData.getIfPresent(aggregateKey, new BigDecimal(0));
		memoryData.addAggregateData(aggregateKey, sumLeftValue.add(sumRightValue));
	}

	@Override
	public Object getAggregateValue(String columnCode, StatisticalData statisticalData) {
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		return statisticalData.getIfPresent(aggregateKey, new BigDecimal(0));
	}


	@Override
	public Object getAggregateValue(String columnCode, List<StatisticalData> statisticalData) {
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		BigDecimal aggregateValue = BigDecimal.ZERO;
		for (StatisticalData data : statisticalData) {
			aggregateValue = aggregateValue.add(data.getIfPresent(aggregateKey, new BigDecimal(0)));
		}
		return aggregateValue;
	}
}
