package db.marmot.statistical;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class AggregateColumn {
	
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
	 * 聚合函数
	 */
	@NotNull
	private Aggregates aggregates;

	public AggregateColumn() {
	}

	public AggregateColumn(String columnCode, ColumnType columnType, Aggregates aggregates) {
		this.columnCode = columnCode;
		this.columnType = columnType;
		this.aggregates = aggregates;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AggregateColumn that = (AggregateColumn) o;
		return Objects.equals(columnCode, that.columnCode) && aggregates == that.aggregates;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode, aggregates);
	}
}
