package db.marmot.graphic.generator.procedure.fetch;

import java.util.List;

import db.marmot.enums.RepositoryType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.volume.CustomRepository;
import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public class TabGraphicCustomFetch extends GraphicCustomFetch<TabGraphic, TabGraphicData> {

    public TabGraphicCustomFetch(RepositoryAdapter repositoryAdapter) {
        super(repositoryAdapter);
    }

    @Override
    public void metadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
        //-自定义不区分明细表和统计表,针对特殊情况的数据获取 保证单一数据结构
        List<FilterColumn> filterColumns = graphic.getGraphicColumn().getFilterColumns();
        CustomRepository customRepository = repositoryAdapter.getRepository(RepositoryType.custom);
        graphicData.setTabData(customRepository.queryData(dataVolume,filterColumns,graphic.getGraphicPage(),graphic.getGraphicLimit()));
    }
}
