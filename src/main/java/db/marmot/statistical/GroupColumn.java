package db.marmot.statistical;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.ColumnType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GroupColumn {
	
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
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GroupColumn that = (GroupColumn) o;
		return Objects.equals(columnCode, that.columnCode);
	}

	public GroupColumn() {
	}

	public GroupColumn(String columnCode, ColumnType columnType) {
		this.columnCode = columnCode;
		this.columnType = columnType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(columnCode);
	}
}
