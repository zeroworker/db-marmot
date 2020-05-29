package db.marmot.graphic.generator.procedure;

import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public abstract class GraphicFormatProcedure<G extends Graphic, D extends GraphicData> implements GraphicProcedure<G, D> {
	
	@Override
	public boolean match(G graphic, DataVolume dataVolume) {
		return graphic.isGraphicFormat();
	}
	
	@Override
	public int getOrder() {
		return 8;
	}
	
	@Override
	public void processed(G graphic, DataVolume dataVolume, D graphicData) {
		//-格式化数据值
		graphicData.formatValueGraphicData();
	}
}
