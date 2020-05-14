package db.marmot.graphic.generator.procedure;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataVolume;

/**
 * 图表数据过滤(针对模型统计的数据)
 * @author shaokang
 */
public abstract class GraphicFilterProcedure<G extends Graphic, D extends GraphicData> implements GraphicProcedure<G, D> {
	
	@Override
	public int getOrder() {
		return 2;
	}
	
	@Override
	public boolean match(G graphic, DataVolume dataVolume) {
		return dataVolume.getVolumeType() == VolumeType.model;
	}
}
