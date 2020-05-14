package db.marmot.graphic.generator.procedure;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataVolume;

/**
 * 图表周期换算(针对模型统计的数据)
 * @author shaokang
 */
public abstract class GraphicCycleProcedure<G extends Graphic, D extends GraphicData> implements GraphicProcedure<G, D> {
	
	@Override
	public int getOrder() {
		return 3;
	}
	
	@Override
	public boolean match(G graphic, DataVolume dataVolume) {
		return dataVolume.getVolumeType() == VolumeType.model;
	}
}
