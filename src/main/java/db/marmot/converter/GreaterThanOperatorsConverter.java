package db.marmot.converter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

/**
 * @author shaokang
 */
public class GreaterThanOperatorsConverter implements OperatorsConverter {
	
	@Override
	public Operators operators() {
		return Operators.greater_than;
	}

	@Override
	public SQLBinaryOperator sqlBinaryOperator() {
		return SQLBinaryOperator.GreaterThan;
	}
	
	@Override
	public void validateValue(ColumnType columnType, Object value) {
		if (columnType != ColumnType.number && columnType != ColumnType.date) {
			throw new ConverterException("大于运算符字段类型只支持数字和时间");
		}
		if (!(value instanceof Number) && !(value instanceof Date)) {
			throw new ConverterException("大于运算符比较值只支持数字和时间");
		}
		if (columnType == ColumnType.number && !(value instanceof Number)) {
			throw new ConverterException("大于运算符字段类型为数字,比较值必须为数字类型");
		}
		if (columnType == ColumnType.date && !(value instanceof Date)) {
			throw new ConverterException("大于运算符字段类型为时间,比较值必须为时间");
		}
	}
	
	@Override
	public void addCondition(MySqlSelectQueryBlock queryBlock, ColumnType columnType, String columnCode, Object value) {
		if (columnType == ColumnType.number) {
			queryBlock.addCondition(columnCode + " > " + ((Number) value).doubleValue());
		}
		if (columnType == ColumnType.date) {
			LocalDate localDate = ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			queryBlock.addCondition(columnCode + " > '" + localDate.format(dateTimeFormatter) + "'");
		}
	}

	@Override
	public boolean compareValue(ColumnType columnType, Object leftValue,Object rightValue) {
		if (leftValue == null || rightValue == null) {
			return false;
		}
		
		if (columnType != ColumnType.number) {
			return new BigDecimal(leftValue.toString()).compareTo(new BigDecimal(rightValue.toString())) > 0;
		}
		
		return ((Date) leftValue).after((Date) rightValue);
	}
}
