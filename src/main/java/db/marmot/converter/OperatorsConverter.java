package db.marmot.converter;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

/**
 * @author shaokang
 */
public interface OperatorsConverter {
	
	/**
	 * 运算符
	 * @return
	 */
	Operators operators();
	
	/**
	 * 运算符
	 * @return
	 */
	SQLBinaryOperator sqlBinaryOperator();
	
	/**
	 * 校验运算符右值
	 */
	void validateValue(ColumnType columnType, Object value);
	
	/**
	 * 添加sql条件
	 * @param queryBlock
	 * @param columnType
	 * @param columnCode
	 * @param value
	 * @return
	 */
	void addCondition(MySqlSelectQueryBlock queryBlock, ColumnType columnType, String columnCode, Object value);
	
	/**
	 * 比较值
	 * @param columnType
	 * @param rightValue
	 * @param leftValue
	 * @return
	 */
	boolean compareValue(ColumnType columnType, Object leftValue,Object rightValue);
}
