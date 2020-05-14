package db.marmot.graphic;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;
import db.marmot.repository.validate.ValidateException;
import db.marmot.statistical.ConditionColumn;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.parser.SelectColumn;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class FilterColumn implements Serializable {
	
	/**
	 * 字段编码
	 */
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z_]+$", message = "必须是英文字母支持大小写以及下划线")
	private String columnCode;
	
	/**
	 * 字段类型
	 */
	@NotNull
	private ColumnType columnType;
	
	/**
	 * 运算符
	 */
	@NotNull
	private Operators operators;
	
	/**
	 * 右值 number list date
	 */
	@NotNull
	private Object rightValue;
	
	public FilterColumn() {
	}
	
	public FilterColumn(String columnCode, ColumnType columnType, Operators operators, Object rightValue) {
		this.columnCode = columnCode;
		this.columnType = columnType;
		this.operators = operators;
		this.rightValue = rightValue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		FilterColumn that = (FilterColumn) o;
		return Objects.equals(columnCode, that.columnCode) && operators == that.operators;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode, operators);
	}
	
	public void validateFilterColumn(DataVolume dataVolume) {
		DataColumn dataColumn = dataVolume.findDataColumn(columnCode);
		
		if (dataColumn == null) {
			throw new ValidateException(String.format("过滤字段%s在数据集字段中不存在", columnCode));
		}
		
		if (columnType != dataColumn.getColumnType()) {
			throw new ValidateException(String.format("过滤字段%s字段类型与数据集字段类型不匹配", columnCode));
		}
		
		ConverterAdapter.getInstance().getOperatorsConverter(operators).validateValue(columnType, rightValue);
	}
	
	/**
	 * 添加条件字段
	 * @param statisticalModel
	 */
	public void addConditionColumn(StatisticalModel statisticalModel, SelectColumn selectColumn) {
		if (columnCode.equals(selectColumn.getColumnAlias())) {
			statisticalModel.getConditionColumns().add(new ConditionColumn(columnCode, columnType, operators, rightValue));
		}
	}
}
