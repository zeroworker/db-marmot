package db.marmot.graphic;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.TotalType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TotalColumn implements Serializable {
	
	private static final long serialVersionUID = -8040111328969296527L;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	private String columnCode;
	
	/**
	 * 合计方式
	 */
	@NotNull
	private TotalType totalType = TotalType.sum;
}
