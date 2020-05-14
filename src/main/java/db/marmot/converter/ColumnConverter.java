package db.marmot.converter;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import db.marmot.enums.ColumnType;

/**
 * @author shaokang
 */
public interface ColumnConverter<V> {
	
	/**
	 * 字段类型
	 * @return
	 */
	Class columnClass();
	
	/**
	 * 字段类型
	 * @return
	 */
	ColumnType columnType();
	
	/**
	 * jdbc types
	 * @return
	 */
	List<JDBCType> jdbcTypes();
	
	/**
	 * 默认数据格式
	 * @return
	 */
	String defaultDataFormat();
	
	/**
	 * 校验字段值
	 * @param clazz
	 * @return
	 */
	boolean validateColumnValue(Class clazz);
	
	/**
	 * 校验字段格式化
	 * @param format
	 * @return
	 */
	void validateColumnFormat(String format);
	
	/**
	 * 格式化字段值
	 * @param value
	 * @param format
	 * @return
	 */
	String formatColumnValue(V value, String format);
	
	/**
	 * 字段值转换
	 * @param value
	 * @return
	 */
	V columnValueConvert(String value);
	
	/**
	 * 字段值转换
	 * @param rs jdbc ResultSet
	 * @param index jdbc ResultSet index
	 * @return
	 * @throws SQLException
	 */
	V columnValueConvert(ResultSet rs, int index) throws SQLException;
}