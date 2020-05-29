package db.marmot.statistical;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Objects;

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
	
	public void validateAggregateColumn(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		dataVolume.findDataColumn(columnCode, columnType);
		ConverterAdapter.getInstance().getAggregatesConverter(aggregates).validateColumnType(columnType);
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
