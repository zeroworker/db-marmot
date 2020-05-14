package db.marmot.graphic.generator;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import db.marmot.volume.DataVolume;

/**
 * 图表数据生成器
 * @author shaokang
 */
public interface GraphicDataGenerator<G extends Graphic, D extends GraphicData> {
	
	/**
	 * 图表类型
	 * @return
	 */
	GraphicType graphicType();
	
	/**
	 * 获取图表数据
	 * @param graphic 图表
	 * @param dataVolume 数据集
	 * @return
	 */
	D getGraphicData(G graphic, DataVolume dataVolume);
}
