package db.marmot.graphic.generator.procedure;

import java.util.HashMap;
import java.util.Map;

import db.marmot.enums.RepositoryType;
import db.marmot.enums.TabGraphicType;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.procedure.fetch.GraphicFetch;
import db.marmot.graphic.generator.procedure.fetch.TabGraphicCustomFetch;
import db.marmot.graphic.generator.procedure.fetch.TabGraphicModelFetch;
import db.marmot.graphic.generator.procedure.fetch.TabGraphicSqlFetch;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;

/**
 * 表格数据抓取
 * @author shaokang
 */
public class TabGraphicFetchProcedure extends GraphicFetchProcedure<TabGraphic, TabGraphicData> {
	
	public TabGraphicFetchProcedure(RepositoryAdapter repositoryAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		super(repositoryAdapter, statisticalGenerateAdapter);
	}
	
	@Override
	protected Map<VolumeType, GraphicFetch> getGraphicFetches(RepositoryAdapter repositoryAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		Map<VolumeType, GraphicFetch> graphicFetches = new HashMap<>();
		graphicFetches.put(VolumeType.model, new TabGraphicModelFetch(repositoryAdapter, statisticalGenerateAdapter));
		graphicFetches.put(VolumeType.sql, new TabGraphicSqlFetch(repositoryAdapter.getRepository(RepositoryType.database)));
		graphicFetches.put(VolumeType.custom, new TabGraphicCustomFetch(repositoryAdapter));
		return graphicFetches;
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		//-模型数据集tab明细强制走sql方式
		if (dataVolume.getVolumeType() == VolumeType.model && graphic.getTabType() == TabGraphicType.detail){
			graphicFetches.get(VolumeType.sql).metadataFetch(graphic, dataVolume, graphicData);
			return;
		}
		super.processed(graphic, dataVolume, graphicData);
	}
}
