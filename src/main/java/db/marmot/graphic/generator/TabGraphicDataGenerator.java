package db.marmot.graphic.generator;

import java.util.ArrayList;
import java.util.List;

import db.marmot.enums.GraphicType;
import db.marmot.enums.TabGraphicType;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.procedure.*;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

/**
 * @author shaokang
 */
public class TabGraphicDataGenerator extends AbstractGraphicDataGenerator<TabGraphic, TabGraphicData> {
	
	public TabGraphicDataGenerator(RepositoryAdapter repositoryAdapter, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		super(repositoryAdapter, columnGeneratorAdapter, statisticalGenerateAdapter);
	}
	
	@Override
	public GraphicType graphicType() {
		return GraphicType.cross_tab;
	}
	
	@Override
	protected TabGraphicData newInstanceGraphicData(TabGraphic graphic, DataVolume dataVolume) {
		TabGraphicData tabGraphicData = new TabGraphicData();
		tabGraphicData.setTabType(graphic.getTabType());
		tabGraphicData.setRowTotal(graphic.getGraphicStyle().isRowTotal());
		tabGraphicData.setSerialNum(graphic.getGraphicStyle().isSerialNum());
		tabGraphicData.setColumnTotal(graphic.getGraphicStyle().isColumnTotal());
		tabGraphicData.setRankColumn(graphic.getTabType() == TabGraphicType.detail ? false : graphic.getGraphicStyle().isRankColumn());
		tabGraphicData.setMergeColumn(graphic.getTabType() == TabGraphicType.detail ? false : graphic.getGraphicStyle().isMergeColumn());
		return tabGraphicData;
	}
	
	@Override
	protected List<GraphicProcedure> getGraphicProcedure(RepositoryAdapter repositoryAdapter, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		List<GraphicProcedure> graphicProcedures = new ArrayList<>();
		graphicProcedures.add(new TabGraphicFetchProcedure(repositoryAdapter, statisticalGenerateAdapter));
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
