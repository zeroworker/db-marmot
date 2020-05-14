package db.marmot.volume.parser;

import java.util.Objects;

import db.marmot.enums.Operators;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class SelectCondition {
	
	/**
	 * 字段表别名
	 */
	private String tableAlias;
	
	/**
	 * 字段编码
	 */
	private String columnCode;
	
	/**
	 * 字段别名
	 */
	private String columnAlias;
	
	/**
	 * 比较符
	 */
	private Operators operators;
	
	/**
	 * 比较右值
	 */
	private Object rightValue;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SelectCondition that = (SelectCondition) o;
		return Objects.equals(tableAlias, that.tableAlias) && Objects.equals(columnCode, that.columnCode) && operators == that.operators;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tableAlias, columnCode, operators);
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SelectCondition{");
		sb.append("tableAlias='").append(tableAlias).append('\'');
		sb.append(", columnCode='").append(columnCode).append('\'');
		sb.append(", operators=").append(operators);
		sb.append(", rightValue=").append(rightValue);
		sb.append('}');
		return sb.toString();
	}
}
