package db.marmot.graphic;

import com.alibaba.fastjson.JSONObject;
import db.marmot.enums.TabGraphicType;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalModelBuilder;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphic extends Graphic {
	
	private static final long serialVersionUID = 5362635861125108894L;
	
	/**
	 * 表格类型
	 */
	@NotNull
	private TabGraphicType tabType = TabGraphicType.detail;
	
	/**
	 * 图表模型
	 */
	@NotNull
	private GraphicModel graphicModel = new GraphicModel();
	
	/**
	 * 表格列
	 */
	@Valid
	@NotNull
	private TabGraphicColumn graphicColumn = new TabGraphicColumn();
	
	/**
	 * 表格样式
	 */
	@Valid
	@NotNull
	private TabGraphicStyle graphicStyle = new TabGraphicStyle();
	
	@Override
	public String toJSONGraphic() {
		return JSONObject.toJSONString(this);
	}
	
	@Override
	public String getModelName() {
		return graphicModel.getModelName();
	}
	
	@Override
	public StatisticalModel configurationModel(StatisticalModelBuilder builder) {
		return null;
	}
	
	@Override
	public void validateGraphic(DataVolume dataVolume) {
		super.validateGraphic(dataVolume);
		graphicStyle.validateGraphicStyle();
		graphicModel.validateGraphicModel(dataVolume);
		graphicColumn.validateFilterColumn(dataVolume);
		graphicColumn.validateDimenColumn(dataVolume, tabType == TabGraphicType.aggregate ? true : false);
		graphicColumn.validateMeasureColumn(dataVolume, tabType == TabGraphicType.aggregate ? true : false);
	}
}
