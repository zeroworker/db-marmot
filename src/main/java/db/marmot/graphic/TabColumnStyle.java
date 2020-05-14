package db.marmot.graphic;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.DataColor;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabColumnStyle implements ColumnStyle {
	
	private static final long serialVersionUID = -6651493317997096270L;
	
	/**
	 * 字段名称
	 */
	@NotBlank
	private String columnName;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	private String columnCode;
	
	/**
	 * 数据颜色
	 */
	@NotNull
	private DataColor dataColor;
}
