package db.marmot.graphic;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.BoardType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.DataVolume;

import lombok.Getter;
import lombok.Setter;

/**
 * 仪表盘
 * @author shaokang
 */
@Setter
@Getter
public class Dashboard {
	
	/**
	 * 仪表盘ID
	 */
	private long boardId;
	
	/**
	 * 数据集ID
	 */
	private long volumeId;
	
	/**
	 * 仪表盘名称
	 */
	@NotBlank
	@Size(max = 128)
	@Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9_.()（）]+$", message = "仪表盘名称只能由中文、英文、数字及和\"_.()（）构成\"")
	private String boardName;

	/**
	 * 仪表盘类型
	 */
	@NotNull
	private BoardType boardType = BoardType.personal;

	/**
	 * 创建人ID
	 */
	@NotBlank
	@Size(max = 512)
	private String founderId;
	
	/**
	 * 创建人名称
	 */
	@NotBlank
	@Size(max = 512)
	private String founderName;

	
	/**
	 * 说明
	 */
	@Size(max = 512)
	private String content;
	
	@Valid
	private List<GraphicDesign> graphicDesigns;
	
	public void validateDashboard(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		if (graphicDesigns != null && !graphicDesigns.isEmpty()) {
			for (int graphicIndex = 0; graphicIndex < graphicDesigns.size(); graphicIndex++) {
				GraphicDesign graphicDesign = graphicDesigns.get(graphicIndex);
				try {
					graphicDesign.validateGraphicDesign(dataVolume);
				} catch (Exception e) {
					throw new ValidateException(String.format("图表%s:%s", graphicDesign.getGraphicName(), e.getMessage()), e);
				}
			}
		}
	}
}
