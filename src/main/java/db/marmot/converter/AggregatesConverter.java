package db.marmot.converter;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.generator.storage.StatisticalStorage;

import java.util.List;

/**
 * @author shaokang
 */
public interface AggregatesConverter {
	
	/**
	 * 聚合函数
	 * @return
	 */
	Aggregates aggregates();
	
	/**
	 * 校验字段类型
	 * @param columnType
	 */
	void validateColumnType(ColumnType columnType);
	
	/**
	 * 添加聚合查询项
	 * @param queryBlock
	 * @param columnCode
	 * @return
	 */
	void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode);
	
	/**
	 * 添加聚合查询项
	 * @param queryBlock
	 * @param expr
	 * @param columnCode
	 * @return
	 */
	void addSelectItem(MySqlSelectQueryBlock queryBlock, String expr, String columnCode);
	
	/**
	 * 计算聚合值
	 * @param statisticalStorage
	 * @param columnCode
	 * @param rightValue
	 * @param direction
	 */
	void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, Object rightValue, boolean direction);
	
	/**
	 * 计算聚合值
	 * @param statisticalStorage
	 * @param rowKey
	 * @param columnCode
	 * @param data
	 */
	void calculate(StatisticalStorage statisticalStorage, String rowKey, String columnCode, StatisticalData data);
	
	/**
	 * 获取统计值
	 * @param columnCode
	 * @param statisticalData
	 * @return
	 */
	Object getAggregateValue(String columnCode, StatisticalData statisticalData);
	
	/**
	 * 获取统计值
	 * @param columnCode
	 * @param statisticalData
	 * @return
	 */
	Object getAggregateValue(String columnCode, List<StatisticalData> statisticalData);
	
}
