package db.marmot.graphic.converter;

import com.alibaba.fastjson.JSONObject;
import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.TabGraphic;

/**
 * @author shaokang
 */
public class TabGraphicConverter implements GraphicConverter {
	
	@Override
	public GraphicType graphicType() {
		return GraphicType.cross_tab;
	}
	
	@Override
	public Graphic parseGraphic(String graphic) {
		return JSONObject.parseObject(graphic, TabGraphic.class);
	}
}
