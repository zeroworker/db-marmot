package db.marmot.graphic.generator.procedure;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.graphic.generator.procedure.fetch.GraphicFetch;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;

import java.util.Map;

/**
 * 图表数据抓取
 * @author shaokang
 */
public abstract class GraphicFetchProcedure<G extends Graphic, D extends GraphicData> implements GraphicProcedure<G, D> {
	
	protected Map<VolumeType, GraphicFetch> graphicFetches;
	
	public GraphicFetchProcedure(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		graphicFetches = getGraphicFetches(dataSourceRepository,statisticalGenerateAdapter);
	}
	
	/**
	 * 注册图表源数据抓取器
	 * @param dataSourceRepository
	 */
	protected abstract Map<VolumeType, GraphicFetch> getGraphicFetches(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter);
	
	@Override
	public int getOrder() {
		return 1;
	}
	
	@Override
	public boolean match(G graphic, DataVolume dataVolume) {
		return true;
	}
	
	@Override
	public void processed(G graphic, DataVolume dataVolume, D graphicData) {
		GraphicFetch graphicFetch = graphicFetches.get(dataVolume.getVolumeType());
		if (graphicFetch == null) {
			throw new GraphicGeneratorException(String.format("图表源数据%s抓取器未实现", dataVolume.getVolumeType().getMessage()));
		}
		graphicFetch.metadataFetch(graphic, dataVolume, graphicData);
	}
}
