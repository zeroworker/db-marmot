package db.marmot.volume;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.ColumnType;

import lombok.Getter;
import lombok.Setter;

/**
 * 表字段
 * @author shaokang
 */
@Setter
@Getter
public class TableColumn {
	
	/**
	 * 表名
	 */
	@NotBlank
	private String tableName;

	/**
	 * 字段编码
	 */
	@NotBlank
	private String columnCode;
	
	/**
	 * 字段类型
	 */
	@NotNull
	private ColumnType columnType;
	
	/**
	 * 别名
	 */
	private String columnAlias;
	
	/**
	 * 描述
	 */
	private String content;

	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TableColumn that = (TableColumn) o;
		if (StringUtils.isNotBlank(columnAlias)) {
			return Objects.equals(tableName, that.tableName) && Objects.equals(columnAlias, that.columnAlias);
		}
		return Objects.equals(tableName, that.tableName) && Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		if (StringUtils.isNotBlank(columnAlias)) {
			return Objects.hash(tableName, columnAlias);
		}
		return Objects.hash(tableName, columnCode);
	}
}
