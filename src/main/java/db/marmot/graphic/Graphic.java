package db.marmot.graphic;

import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalModelBuilder;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class Graphic implements Serializable {
	
	/**
	 * 图表数据预览页数
	 */
	@Size(min = 1)
	private int graphicPage = 1;
	
	/**
	 * 表格图表预览行数
	 */
	@Size(max = 10000)
	private int graphicLimit = 10000;
	
	/**
	 * 是否格式化图表数据 数据展示需要格式化,数据导出无需格式化
	 */
	private boolean graphicFormat = Boolean.TRUE;
	
	/**
	 * 是否实时图表,数据源为模型统计时,不支持实时图表,实时数据为mock数据
	 */
	private boolean graphicInstant = Boolean.TRUE;
	
	/**
	 * 序列化图表
	 * @return
	 */
	public abstract String toJSONGraphic();
	
	/**
	 * 获取模型名称
	 * @return
	 */
	public abstract String getModelName();
	
	/**
	 * 配置模型
	 * @param builder
	 * @return
	 */
	public abstract StatisticalModel configurationModel(StatisticalModelBuilder builder);
	
	/**
	 * 验证图表
	 * @param dataVolume
	 */
	public void validateGraphic(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		if (dataVolume.getVolumeType() == VolumeType.model) {
			if (this.graphicLimit > 1000) {
				throw new ValidateException("模型数据源图表数据最大支持预览1000");
			}
		}
	}
	
	/**
	 * 图表数据下页
	 */
	public void nextGraphicPage() {
		this.graphicPage = this.graphicPage + 1;
	}
	
}
