package db.marmot.converter;

import db.marmot.enums.*;
import db.marmot.graphic.converter.GraphicCycleConverter;

import java.util.Map;

/**
 * @author shaokang
 */
public interface SelectSqlBuilderConverter {
	
	/**
	 * 设置比较符转换器
	 * @param operatorsConverters
	 */
	void setOperatorsConverters(Map<Operators, OperatorsConverter> operatorsConverters);
	
	/**
	 * 设置聚合转换器
	 * @param aggregatesConverters
	 */
	void setAggregatesConverters(Map<Aggregates, AggregatesConverter> aggregatesConverters);
	
	/**
	 * 设置时间周期转换器
	 * @param graphicCycleConverters
	 */
	void setGraphicCycleConverters(Map<GraphicCycle, GraphicCycleConverter> graphicCycleConverters);
	
	/**
	 * 添加查询table
	 * @return
	 */
	SelectSqlBuilderConverter addSelectTable(String table);

	/**
	 * 添加查询table
	 * @return
	 */
	SelectSqlBuilderConverter addSelectTable(String table,String alias);

	/**
	 * 添加条件
	 * @param operators 运算符
	 * @param columnType 字段类型
	 * @param columnCode 字段
	 * @param value 值
	 * @return
	 */
	SelectSqlBuilderConverter addCondition(Operators operators, ColumnType columnType, String columnCode, Object value);
	
	/**
	 * 添加数字条件
	 * @param operators 运算符
	 * @param columnCode 字段
	 * @param unitValue 换算单位
	 * @param value 值
	 * @return
	 */
	SelectSqlBuilderConverter addNumberCondition(Operators operators, String columnCode, double unitValue, Object value);
	
	/**
	 * 添加字段
	 * @param columnCode 字段
	 * @return
	 */
	SelectSqlBuilderConverter addSelectItem(String columnCode);
	
	/**
	 * 添加表达式字段
	 * @param expr
	 * @param columnCode
	 * @return
	 */
	SelectSqlBuilderConverter addSelectExprItem(String expr, String columnCode);
	
	/**
	 * 添加时间字段
	 * @param columnCode 字段
	 * @param dateCycle 周期
	 * @return
	 */
	SelectSqlBuilderConverter addSelectDateCycleItem(String columnCode, GraphicCycle graphicCycle);
	
	/**
	 * 添加数字字段
	 * @param columnCode 字段
	 * @param unitValue 换算单位
	 * @return
	 */
	SelectSqlBuilderConverter addSelectNumberItem(String columnCode, double unitValue);
	
	/**
	 * 添加聚合字段
	 * @param aggregates
	 * @param columnCode
	 * @param unitValue
	 * @return
	 */
	SelectSqlBuilderConverter addSelectAggregateItem(Aggregates aggregates, String columnCode, double unitValue);
	
	/**
	 * 添加聚合字段
	 * @param aggregates
	 * @param expr
	 * @param columnCode
	 * @return
	 */
	SelectSqlBuilderConverter addSelectAggregateItem(Aggregates aggregates, String expr, String columnCode);
	
	/**
	 * 添加排序字段
	 * @param columnCode
	 * @param orderType
	 * @return
	 */
	SelectSqlBuilderConverter addOrderBy(String columnCode, OrderType orderType);
	
	/**
	 * 添加分组字段
	 * @param columnCode
	 * @return
	 */
	SelectSqlBuilderConverter addGroupBy(String columnCode);
	
	/**
	 * 添加时间分组字段
	 * @param columnCode
	 * @return
	 */
	SelectSqlBuilderConverter addDateCycleGroupBy(String columnCode, GraphicCycle graphicCycle);
	
	/**
	 * 分页分页
	 * @param pageNum 分页数
	 * @param pageSize 每页大小
	 * @return
	 */
	MySqlSelectSqlBuilderConverter addLimit(int pageNum, int pageSize);
	
	/**
	 * 获取sql
	 * @return
	 */
	String toSql();
	
}
