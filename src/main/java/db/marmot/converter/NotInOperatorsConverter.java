package db.marmot.converter;

import java.util.List;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

/**
 * @author shaokang
 */
public class NotInOperatorsConverter implements OperatorsConverter {
	
	@Override
	public Operators operators() {
		return Operators.not_in;
	}
	
	@Override
	public SQLBinaryOperator sqlBinaryOperator() {
		return null;
	}
	
	@Override
	public void validateValue(ColumnType columnType, Object value) {
		if (columnType != ColumnType.string) {
			throw new ConverterException("不包含运算符只支持文本类型字段");
		}
		if (!(value instanceof String) && !(value instanceof List)) {
			throw new ConverterException("不包含运算符比较值只支持文本或者文本集合");
		}
		if (value instanceof List) {
			Object obj = ((List) value).get(0);
			if (!(obj instanceof String)) {
				throw new ConverterException("不包含运算符比较值集合值必须为文本");
			}
		}
	}
	
	@Override
	public void addCondition(MySqlSelectQueryBlock queryBlock, ColumnType columnType, String columnCode, Object value) {
		if (value instanceof String) {
			queryBlock.addCondition(columnCode + " not in ('" + value.toString() + "')");
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (Object obj : (List) value) {
			builder.append("'").append(obj.toString()).append("',");
		}
		String condition = builder.toString().substring(0, builder.length() - 1);
		queryBlock.addCondition(columnCode + " not in ('" + condition + "')");
	}
	
	@Override
	public boolean compareValue(ColumnType columnType, Object leftValue, Object rightValue) {
		if (leftValue == null || rightValue == null) {
			return false;
		}
		
		if (rightValue instanceof List) {
			return ((List) rightValue).contains(leftValue);
		}
		
		return leftValue.toString().indexOf(rightValue.toString()) < 0;
	}
}
