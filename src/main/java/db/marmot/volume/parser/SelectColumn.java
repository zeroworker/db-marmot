package db.marmot.volume.parser;

import java.util.Objects;

import db.marmot.repository.validate.Validators;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class SelectColumn {
	
	/**
	 * 字段编码
	 */
	private String columnCode;
	
	/**
	 * 字段别名
	 */
	private String columnAlias;
	
	/**
	 * 字段表别名
	 */
	private String tableAlias;
	
	public SelectColumn(String columnAlias) {
		Validators.notNull(columnAlias, "字段表别名不能为空");
		this.columnAlias = columnAlias;
	}
	
	public SelectColumn(String columnCode, String columnAlias, String tableAlias) {
		
		Validators.notNull(columnCode, "字段名不能为空");
		Validators.notNull(columnAlias, "字段别名不能为空");
		Validators.notNull(tableAlias, "字段表别名不能为空");
		
		this.columnCode = columnCode;
		this.columnAlias = columnAlias;
		this.tableAlias = tableAlias;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SelectColumn that = (SelectColumn) o;
		return Objects.equals(columnAlias, that.columnAlias);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnAlias);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SelectColumn{");
		sb.append("columnCode='").append(columnCode).append('\'');
		sb.append(", columnAlias='").append(columnAlias).append('\'');
		sb.append(", tableAlias='").append(tableAlias).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
