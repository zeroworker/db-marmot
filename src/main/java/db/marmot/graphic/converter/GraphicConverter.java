package db.marmot.graphic.converter;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;

/**
 * @author shaokang
 */
public interface GraphicConverter {
	
	/**
	 * 图表类型
	 * @return
	 */
	GraphicType graphicType();
	
	/**
	 * 解析图表设计
	 * @param graphic
	 * @return
	 */
	Graphic parseGraphic(String graphic);
}