package db.marmot.graphic.generator;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;

/**
 * 图表生成适配器
 * @author shaokang
 */
public interface GraphicGeneratorAdapter {
	
	/**
	 * 生成图表数据
	 * @param graphicCode 图表编码
	 * @param graphicFormat 格式化图表数据
	 * @return
	 */
	<D extends GraphicData> D generateGraphicData(String graphicCode, boolean graphicFormat);
	
	/**
	 * 生成图表数据 图表格式化以及实时图表设置可以直接设置graphic
	 * @param volumeCode 数据集编码
	 * @param graphicType 图表类型
	 * @param graphic 图表
	 * @return
	 */
	<D extends GraphicData> D generateGraphicData(String volumeCode, GraphicType graphicType, Graphic graphic);
}
