package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.repository.DataSourceRepository;
import db.marmot.volume.DataVolume;

import java.util.List;

/**
 * @author shaokang
 */
public class TabGraphicCustomFetch extends GraphicCustomFetch<TabGraphic, TabGraphicData> {
	
	public TabGraphicCustomFetch(DataSourceRepository dataSourceRepository) {
		super(dataSourceRepository);
	}
	
	@Override
	public void metadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		List<FilterColumn> filterColumns = graphic.getGraphicColumn().getFilterColumns();
		graphicData.setData(dataSourceRepository.queryCustomData(dataVolume.getVolumeCode(), filterColumns, graphic.getGraphicPage(), graphic.getGraphicLimit()));
	}
}
