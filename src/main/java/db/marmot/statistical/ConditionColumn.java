package db.marmot.statistical;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class ConditionColumn {
	
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

	public ConditionColumn() {
	}

	public ConditionColumn(String columnCode, ColumnType columnType, Operators operators, Object rightValue) {
		this.columnCode = columnCode;
		this.columnType = columnType;
		this.operators = operators;
		this.rightValue = rightValue;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConditionColumn that = (ConditionColumn) o;
		return Objects.equals(columnCode, that.columnCode) && operators == that.operators;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode, operators);
	}
}
