package db.marmot.converter;

import java.util.List;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

/**
 * @author shaokang
 */
public class InterSectionOperatorsConverter implements OperatorsConverter {
	
	@Override
	public Operators operators() {
		return Operators.inter_section;
	}
	
	@Override
	public SQLBinaryOperator sqlBinaryOperator() {
		return null;
	}
	
	@Override
	public void validateValue(ColumnType columnType, Object value) {
		if (columnType != ColumnType.string) {
			throw new ConverterException("交集运算符只支持文本类型字段");
		}
		if (!(value instanceof String) && !(value instanceof List)) {
			throw new ConverterException("交集运算符比较值只支持文本或者文本集合");
		}
		if (value instanceof List) {
			Object obj = ((List) value).get(0);
			if (!(obj instanceof String)) {
				throw new ConverterException("交集运算符比较值集合值必须为文本");
			}
		}
	}
	
	@Override
	public void addCondition(MySqlSelectQueryBlock queryBlock, ColumnType columnType, String columnCode, Object value) {
		if (value instanceof String) {
			queryBlock.addCondition(columnCode + " like '%" + value.toString() + "%'");
			return;
		}
		StringBuilder builder = new StringBuilder();
		
		List list = (List) value;
		for (int i = 0; i < list.size(); i++) {
			builder.append(columnCode).append(" like '%").append(list.get(i)).append("%'");
			if (i + 1 < list.size()) {
				builder.append(" or ");
			}
		}
		
		queryBlock.addCondition(" (" + builder.toString() + ")");
	}
	
	@Override
	public boolean compareValue(ColumnType columnType, Object leftValue, Object rightValue) {
		if (leftValue == null || rightValue == null) {
			return false;
		}
		
		if (leftValue instanceof List) {
			for (Object value : (List) rightValue) {
				return leftValue.toString().indexOf(value.toString()) >= 0;
			}
		}
		
		return leftValue.toString().indexOf(rightValue.toString()) >= 0;
	}
}
