package db.marmot.converter;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;

/**
 * @author shaokang
 */
public class DateColumnConverter implements ColumnConverter<Date> {
	
	private Pattern dateFormatPattern = Pattern.compile("[yMdHms\\-\\:\\/\\s]*");
	
	private List<JDBCType> jdbcTypes = Lists.newArrayList();
	
	public DateColumnConverter() {
		jdbcTypes.add(JDBCType.DATE);
		jdbcTypes.add(JDBCType.TIME);
		jdbcTypes.add(JDBCType.TIMESTAMP);
	}
	
	@Override
	public Class columnClass() {
		return Date.class;
	}
	
	@Override
	public ColumnType columnType() {
		return ColumnType.date;
	}
	
	@Override
	public List<JDBCType> jdbcTypes() {
		return jdbcTypes;
	}
	
	@Override
	public String defaultDataFormat() {
		return "yyyy-MM-dd HH:mm:ss";
	}
	
	@Override
	public boolean validateColumnValue(Class clazz) {
		return clazz.equals(Date.class);
	}
	
	@Override
	public void validateColumnFormat(String format) {
		if (!dateFormatPattern.matcher(format).matches()) {
			throw new ConverterException("时间类型格式化格式不正确");
		}
	}
	
	@Override
	public String formatColumnValue(Date value, String format) {
		format = StringUtils.isBlank(format) ? defaultDataFormat() : format;
		LocalDate localDate = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return dateTimeFormatter.format(localDate);
	}
	
	@Override
	public Date columnValueConvert(String value) {
		try {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(defaultDataFormat());
			LocalDateTime localDateTime = LocalDateTime.parse(value, dateTimeFormatter);
			return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		} catch (Exception e) {
			throw new ConverterException(String.format("时间值%s格式必须为%s", value, defaultDataFormat()));
		}
	}
	
	@Override
	public Date columnValueConvert(ResultSet rs, int index) throws SQLException {
		return rs.getDate(index);
	}
}
