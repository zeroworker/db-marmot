package db.marmot.graphic.generator;

import db.marmot.enums.GraphicType;
import db.marmot.enums.GraphicLayout;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.procedure.*;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
public class TabGraphicDataGenerator extends AbstractGraphicDataGenerator<TabGraphic, TabGraphicData> {
	
	public TabGraphicDataGenerator(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		super(dataSourceRepository, columnGeneratorAdapter, statisticalGenerateAdapter);
	}
	
	@Override
	public GraphicType graphicType() {
		return GraphicType.cross_tab;
	}
	
	@Override
	protected TabGraphicData newInstanceGraphicData(TabGraphic graphic, DataVolume dataVolume) {
		TabGraphicData tabGraphicData = new TabGraphicData();
		tabGraphicData.setGraphicLayout(graphic.getGraphicLayout());
		tabGraphicData.setRowTotal(graphic.getGraphicStyle().isRowTotal());
		tabGraphicData.setSerialNum(graphic.getGraphicStyle().isSerialNum());
		tabGraphicData.setColumnTotal(graphic.getGraphicStyle().isColumnTotal());
		tabGraphicData.setRankColumn(graphic.getGraphicLayout() == GraphicLayout.detail ? false : graphic.getGraphicStyle().isRankColumn());
		tabGraphicData.setMergeColumn(graphic.getGraphicLayout() == GraphicLayout.detail ? false : graphic.getGraphicStyle().isMergeColumn());
		return tabGraphicData;
	}
	
	@Override
	protected List<GraphicProcedure> getGraphicProcedure(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		List<GraphicProcedure> graphicProcedures = new ArrayList<>();
		graphicProcedures.add(new TabGraphicFetchProcedure(dataSourceRepository, statisticalGenerateAdapter));
		graphicProcedures.add(new TabGraphicCycleProcedure());
		graphicProcedures.add(new TabGraphicFilterProcedure());
		graphicProcedures.add(new TabGraphicStructureProcedure(columnGeneratorAdapter));
		graphicProcedures.add(new TabGraphicSubtotalProcedure());
		graphicProcedures.add(new TabGraphicTotalProcedure());
		graphicProcedures.add(new TabGraphicRankProcedure());
		graphicProcedures.add(new TabGraphicFormatProcedure());
		return graphicProcedures;
	}
}
