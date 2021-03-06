package db.marmot.graphic;

import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.DirectionColumn;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.mvel2.MVEL;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GraphicModel implements Serializable {
	
	/**
	 * 模型名称
	 */
	@NotBlank
	private String modelName;
	
	/**
	 * 图表偏移量表达式 模型统计生效,sql统计直接使用时间维度过滤即可
	 */
	@NotBlank
	private String offsetExpr = "0*60*60*1000+0*60*1000+0*1000";
	
	/**
	 * 统计方向
	 */
	@NotNull
	private List<DirectionColumn> directionColumns = new ArrayList<>();
	
	public void validateGraphicModel(DataVolume dataVolume) {
		if (dataVolume.getVolumeType() == VolumeType.model) {
			Validators.assertJSR303(this);
			if (StringUtils.isNotBlank(offsetExpr)) {
				try {
					Integer.valueOf(MVEL.eval(offsetExpr).toString());
				} catch (Exception e) {
					throw new ValidateException(String.format("无法解析偏移量表达式:%s", offsetExpr));
				}
			}
			directionColumns.forEach(directionColumn -> directionColumn.validateDirectionColumn(dataVolume));
		}
	}
}
