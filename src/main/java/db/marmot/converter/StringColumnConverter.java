package db.marmot.converter;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;

/**
 * @author shaokang
 */
public class StringColumnConverter implements ColumnConverter<String> {
	
	private List<JDBCType> jdbcTypes = Lists.newArrayList();
	
	public StringColumnConverter() {
		jdbcTypes.add(JDBCType.CHAR);
		jdbcTypes.add(JDBCType.VARCHAR);
		jdbcTypes.add(JDBCType.LONGVARCHAR);
	}
	
	@Override
	public Class columnClass() {
		return String.class;
	}
	
	@Override
	public ColumnType columnType() {
		return ColumnType.string;
	}
	
	@Override
	public List<JDBCType> jdbcTypes() {
		return jdbcTypes;
	}
	
	@Override
	public String defaultDataFormat() {
		return "TEXT";
	}
	
	@Override
	public boolean validateColumnValue(Class clazz) {
		return clazz.equals(String.class);
	}
	
	@Override
	public void validateColumnFormat(String format) {
		if (!"TEXT".equals(format)) {
			throw new ConverterException("文本字段格式化必须为TEXT");
		}
	}
	
	@Override
	public String formatColumnValue(String value, String format) {
		return value;
	}
	
	@Override
	public String columnValueConvert(String value) {
		return value;
	}
	
	@Override
	public String columnValueConvert(ResultSet rs, int index) throws SQLException {
		String value = rs.getString(index);
		return StringUtils.isBlank(value) ? "" : value;
	}
}
