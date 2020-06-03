package db.marmot.converter;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.generator.storage.StatisticalStorage;

/**
 * @author shaokang
 */
public class CountAggregatesConverter implements AggregatesConverter {
	
	@Override
	public Aggregates aggregates() {
		return Aggregates.count;
	}
	
	@Override
	public void validateColumnType(ColumnType columnType) {
		//-支持任意字段类型
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("count(" + columnCode + ")"), columnCode);
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String expr, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("count(" + expr + ")"), columnCode);
	}
	
	@Override
	public void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, Object rightValue, boolean direction) {
		StatisticalData statisticalData = statisticalStorage.getStatisticalData(rowKey);
		
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		BigDecimal addValue = direction ? new BigDecimal("1") : new BigDecimal("-1");
		BigDecimal leftValue = statisticalData.putIfPresent(aggregateKey, new BigDecimal(0));
		statisticalData.addAggregateData(aggregateKey, leftValue.add(addValue));
	}
	
	@Override
	public void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, StatisticalData data) {
		String aggregateKey = StringUtils.join(aggregates().getCode(), "@", columnCode);
		
		StatisticalData memoryData = statisticalStorage.getStatisticalData(rowKey);
		BigDecimal leftValue = memoryData.getIfPresent(aggregateKey, new BigDecimal(0));
		BigDecimal addValue = data.getIfPresent(aggregateKey, new BigDecimal(0));
		memoryData.addAggregateData(aggregateKey, leftValue.add(addValue));
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
