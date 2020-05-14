package db.marmot.graphic.generator.procedure;

import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataVolume;

/**
 * 表格数据格式化处理以及层级处理
 * @author shaokang
 */
public class TabGraphicFormatProcedure extends GraphicFormatProcedure<TabGraphic, TabGraphicData> {
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		return super.match(graphic, dataVolume) || graphic.getGraphicStyle().isRankColumn();
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		//-格式化数据值
		graphicData.formatValueTabGraphicData();
		//-转换成树状结构数据
		graphicData.formatTreeTabGraphicData(graphic);
	}
}
