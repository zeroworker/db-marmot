package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.converter.ConverterAdapter;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public abstract class GraphicModelFetch<G extends Graphic, D extends GraphicData> implements GraphicFetch<G, D> {

	protected DataSourceRepository dataSourceRepository;
	protected StatisticalGenerateAdapter statisticalGenerateAdapter;
	protected ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public GraphicModelFetch(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		this.dataSourceRepository = dataSourceRepository;
		this.statisticalGenerateAdapter = statisticalGenerateAdapter;
	}
	
	@Override
	public void metadataFetch(G graphic, DataVolume dataVolume, D graphicData) {
		if (graphic.isGraphicInstant()) {
			mockMetadataFetch(graphic, dataVolume, graphicData);
			return;
		}
		modelMetadataFetch(graphic, dataVolume, graphicData);
	}
	
	/**
	 * 模拟数据抓取
	 * @param graphic
	 * @param dataVolume
	 * @param graphicData
	 */
	public abstract void mockMetadataFetch(G graphic, DataVolume dataVolume, D graphicData);
	
	/**
	 * 模型源数据抓取
	 * @param graphic
	 * @param dataVolume
	 * @param graphicData
	 */
	public abstract void modelMetadataFetch(G graphic, DataVolume dataVolume, D graphicData);
	
}
