package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public interface GraphicFetch<G extends Graphic, D extends GraphicData> {
	
	/**
	 * 获取源数据
	 * @param graphic
	 * @param dataVolume
	 * @param graphicData
	 */
	void metadataFetch(G graphic, DataVolume dataVolume, D graphicData);
}
