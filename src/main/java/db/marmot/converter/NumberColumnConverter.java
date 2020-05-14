package db.marmot.converter;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;

/**
 * @author shaokang
 */
public class NumberColumnConverter implements ColumnConverter<BigDecimal> {
	
	private Pattern numberFormatPattern = Pattern.compile("^#+[0]*[.]*[0]*[#]*[%‰]*");
	
	private List<JDBCType> jdbcTypes = Lists.newArrayList();
	
	public NumberColumnConverter() {
		jdbcTypes.add(JDBCType.FLOAT);
		jdbcTypes.add(JDBCType.BIGINT);
		jdbcTypes.add(JDBCType.DOUBLE);
		jdbcTypes.add(JDBCType.INTEGER);
		jdbcTypes.add(JDBCType.DECIMAL);
	}
	
	@Override
	public Class columnClass() {
		return BigDecimal.class;
	}
	
	@Override
	public ColumnType columnType() {
		return ColumnType.number;
	}
	
	@Override
	public List<JDBCType> jdbcTypes() {
		return jdbcTypes;
	}
	
	@Override
	public String defaultDataFormat() {
		return "###0.00";
	}
	
	@Override
	public boolean validateColumnValue(Class clazz) {
		return clazz.equals(BigDecimal.class);
	}
	
	@Override
	public void validateColumnFormat(String format) {
		if (!numberFormatPattern.matcher(format).matches()) {
			throw new ConverterException("数字类型格式化格式不正确");
		}
	}
	
	@Override
	public String formatColumnValue(BigDecimal value, String format) {
		format = StringUtils.isBlank(format) ? defaultDataFormat() : format;
		DecimalFormat decimalFormat = new DecimalFormat(format);
		return decimalFormat.format(value);
	}
	
	@Override
	public BigDecimal columnValueConvert(String value) {
		try {
			return new BigDecimal(value);
		} catch (Exception e) {
			throw new ConverterException(String.format("数字值%s必须为数字", value));
		}
	}
	
	@Override
	public BigDecimal columnValueConvert(ResultSet rs, int index) throws SQLException {
		BigDecimal value = rs.getBigDecimal(index);
		return value == null ? BigDecimal.ZERO : value;
	}
}
