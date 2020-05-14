package db.marmot.graphic.generator.procedure;

import org.springframework.core.Ordered;

import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataVolume;

/**
 * 数据处理
 * @author shaokang
 */
public interface GraphicProcedure<G extends Graphic, D extends GraphicData> extends Ordered {
	
	/**
	 * 匹配是否执行图表生成流程
	 * @param graphic
	 * @param dataVolume
	 * @return
	 */
	boolean match(G graphic, DataVolume dataVolume);
	
	/**
	 * 执行图表生成流程
	 * @param graphic
	 * @param dataVolume
	 * @param graphicData
	 */
	void processed(G graphic, DataVolume dataVolume, D graphicData);
}
