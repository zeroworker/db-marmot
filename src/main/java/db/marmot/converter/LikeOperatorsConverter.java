package db.marmot.converter;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

/**
 * @author shaokang
 */
public class LikeOperatorsConverter implements OperatorsConverter {
	
	@Override
	public Operators operators() {
		return Operators.like;
	}
	
	@Override
	public SQLBinaryOperator sqlBinaryOperator() {
		return SQLBinaryOperator.Like;
	}
	
	@Override
	public void validateValue(ColumnType columnType, Object value) {
		if (columnType != ColumnType.string) {
			throw new ConverterException("模糊匹配运算符只支持文本类型字段");
		}
		if (!(value instanceof String)) {
			throw new ConverterException("模糊匹配运算符比较值只支持文本");
		}
	}
	
	@Override
	public void addCondition(MySqlSelectQueryBlock queryBlock, ColumnType columnType, String columnCode, Object value) {
		queryBlock.addCondition(columnCode + " like '%" + value.toString() + "%'");
	}
	
	@Override
	public boolean compareValue(ColumnType columnType, Object leftValue, Object rightValue) {
		if (leftValue == null || rightValue == null) {
			return false;
		}
		return leftValue.toString().indexOf(rightValue.toString()) >= 0;
	}
}
