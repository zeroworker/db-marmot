package db.marmot.graphic;

import db.marmot.enums.GraphicType;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 图表设计
 * @author shaokang
 */
@Setter
@Getter
public class GraphicDesign {
	
	/**
	 * 图表ID
	 */
	private long graphicId;
	
	/**
	 * 图表名称
	 */
	@NotBlank
	@Size(max = 128)
	@Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9_.()（）]+$", message = "图表名称只能由中文、英文、数字及和\"_.()（）构成\"")
	private String graphicName;
	
	/**
	 * 图表编码
	 */
	private String graphicCode;
	
	/**
	 * 仪表盘ID
	 */
	private long boardId;
	
	/**
	 * 图表类型
	 */
	@NotNull
	private GraphicType graphicType;
	
	/**
	 * 图表
	 */
	@NotNull
	@Valid
	private Graphic graphic;
	
	public GraphicDesign createGraphicCode() {
		this.graphicCode = DigestUtils.md5Hex(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + RandomStringUtils.randomAscii(5));
		return this;
	}
	
	public void validateGraphicDesign(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		graphic.validateGraphic(dataVolume);
	}
}
