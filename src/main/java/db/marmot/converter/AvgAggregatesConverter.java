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
public class AvgAggregatesConverter implements AggregatesConverter {
	
	@Override
	public Aggregates aggregates() {
		return Aggregates.avg;
	}
	
	@Override
	public void validateColumnType(ColumnType columnType) {
		if (columnType != ColumnType.number) {
			throw new ConverterException("聚合函数平均值只支持数字类型字段");
		}
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("avg(" + columnCode + ")"), columnCode);
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String expr, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("avg(" + expr + ")"), columnCode);
	}
	
	@Override
	public void calculate(TemporaryMemory temporaryMemory, String rowKey, String columnCode, Object rightValue, boolean direction) {
		StatisticalData statisticalData = temporaryMemory.getStatisticalData(rowKey);
		
		String countKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.count.getCode(), "_", columnCode);
		BigDecimal countRightValue = direction ? new BigDecimal("1") : new BigDecimal("-1");
		BigDecimal countLeftValue = statisticalData.putIfPresent(countKey, new BigDecimal(0));
		statisticalData.addAggregateData(countKey, countLeftValue.add(countRightValue));
		
		String sumKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.sum.getCode(), "_", columnCode);
		BigDecimal sumLeftValue = statisticalData.putIfPresent(sumKey, new BigDecimal(0));
		BigDecimal sumRightValue = direction ? (BigDecimal) rightValue : ((BigDecimal) rightValue).negate();
		statisticalData.addAggregateData(sumKey, sumLeftValue.add(sumRightValue));
		
	}
	
	@Override
	public void calculate(TemporaryMemory temporaryMemory, String rowKey, String columnCode, StatisticalData data) {
		StatisticalData memoryData = temporaryMemory.getStatisticalData(rowKey);
		
		String countKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.count.getCode(), "_", columnCode);
		BigDecimal countLeftValue = memoryData.getIfPresent(countKey, new BigDecimal(0));
		BigDecimal countRightValue = data.getIfPresent(countKey, new BigDecimal(0));
		memoryData.addAggregateData(countKey, countLeftValue.add(countRightValue));
		
		String sumKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.sum.getCode(), "_", columnCode);
		BigDecimal sumLeftValue = memoryData.getIfPresent(sumKey, new BigDecimal(0));
		BigDecimal sumRightValue = data.getIfPresent(sumKey, new BigDecimal(0));
		memoryData.addAggregateData(countKey, sumLeftValue.add(sumRightValue));
	}
	
	@Override
	public Object getAggregateValue(String columnCode, StatisticalData statisticalData) {
		String countKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.count.getCode(), "_", columnCode);
		BigDecimal countValue = statisticalData.getIfPresent(countKey, new BigDecimal(0));
		
		String sumKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.sum.getCode(), "_", columnCode);
		BigDecimal sumValue = statisticalData.getIfPresent(sumKey, new BigDecimal(0));
		
		return countValue.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : sumValue.divide(countValue);
	}
	
	@Override
	public Object getAggregateValue(String columnCode, List<StatisticalData> statisticalData) {
		
		String sumKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.sum.getCode(), "_", columnCode);
		String countKey = StringUtils.join(aggregates().getCode(), "@", Aggregates.count.getCode(), "_", columnCode);
		
		BigDecimal sumValue = BigDecimal.ZERO;
		BigDecimal countValue = BigDecimal.ZERO;
		for (StatisticalData data : statisticalData) {
			sumValue = sumValue.add(data.getIfPresent(sumKey, new BigDecimal(0)));
			countValue = countValue.add(data.getIfPresent(countKey, new BigDecimal(0)));
		}
		
		return countValue.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : sumValue.divide(countValue);
	}
}
