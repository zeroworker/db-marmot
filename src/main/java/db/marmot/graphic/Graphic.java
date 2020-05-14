package db.marmot.graphic;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataVolume;

import lombok.Getter;
import lombok.Setter;

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
	 * 图表偏移量表达式 模型统计生效,sql统计直接使用时间维度过滤即可
	 */
	private String offsetExpr = "0*60*60*1000+0*60*1000+0*1000";
	
	/**
	 * 获取统计模型名称
	 * @return
	 */
	public abstract List<String> getModelNames();
	
	/**
	 * 创建统计模型
	 * @param dataVolume
	 * @param graphicName
	 * @param dbType
	 * @return
	 */
	public abstract List<StatisticalModel> createStatisticalModels(DataVolume dataVolume,String dbType,String graphicName);
	
	/**
	 * 序列化图表
	 * @return
	 */
	public abstract String toJSONGraphic();
	
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
			
			Validators.notNull(offsetExpr, "offsetExpr 不能为空");
			
			try {
				Integer.valueOf(ConverterAdapter.getInstance().eval(offsetExpr).toString());
			} catch (Exception e) {
				throw new ValidateException(String.format("无法解析偏移量表达式:%s", offsetExpr));
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
