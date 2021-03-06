package db.marmot.converter;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.generator.storage.StatisticalStorage;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shaokang
 */
public class MaxAggregatesConverter implements AggregatesConverter {
	@Override
	public Aggregates aggregates() {
		return Aggregates.max;
	}
	
	@Override
	public void validateColumnType(ColumnType columnType) {
		if (columnType != ColumnType.number) {
			throw new ConverterException("聚合函数最大值只支持数字类型字段");
		}
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("max(" + columnCode + ")"), columnCode);
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String expr, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("max(" + expr + ")"), columnCode);
	}
	
	@Override
	public void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, Object rightValue, boolean direction) {
		StatisticalData statisticalData = statisticalStorage.getStatisticalData(rowKey);
		
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		BigDecimal compareValue = direction ? (BigDecimal) rightValue : ((BigDecimal) rightValue).negate();
		BigDecimal leftValue = statisticalData.putIfPresent(aggregateKey, new BigDecimal(0));
		statisticalData.addAggregateData(aggregateKey, leftValue.max(compareValue));
	}
	
	@Override
	public void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, StatisticalData data) {
		StatisticalData memoryData = statisticalStorage.getStatisticalData(rowKey);
		
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		BigDecimal leftValue = memoryData.getIfPresent(aggregateKey, new BigDecimal(0));
		BigDecimal compareValue = data.getIfPresent(aggregateKey, new BigDecimal(0));
		memoryData.addAggregateData(aggregateKey, leftValue.max(compareValue));
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
			aggregateValue = aggregateValue.max(data.getIfPresent(aggregateKey, new BigDecimal(0)));
		}
		return aggregateValue;
	}
}
