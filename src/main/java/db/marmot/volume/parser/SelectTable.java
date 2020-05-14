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
public class SelectTable {
	
	/**
	 * 表名称
	 */
	private String tableName;
	
	/**
	 * 表别名
	 */
	private String tableAlias;
	
	public SelectTable(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public SelectTable(String tableName, String tableAlias) {
		Validators.notNull(tableName, "表名不能为空");
		Validators.notNull(tableAlias, "表别名名不能为空");
		
		this.tableName = tableName;
		this.tableAlias = tableAlias;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SelectTable that = (SelectTable) o;
		return Objects.equals(tableAlias, that.tableAlias);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tableAlias);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SelectTable{");
		sb.append("tableName='").append(tableName).append('\'');
		sb.append(", tableAlias='").append(tableAlias).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
