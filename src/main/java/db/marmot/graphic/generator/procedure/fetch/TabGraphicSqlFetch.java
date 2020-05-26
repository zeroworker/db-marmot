package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.enums.TabGraphicType;
import db.marmot.graphic.*;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataBaseRepository;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shaokang
 */
public class TabGraphicSqlFetch extends GraphicSqlFetch<TabGraphic, TabGraphicData> {
	
	private Map<TabGraphicType, TabGraphicSqlBuilder> tabGraphicSqlBuilders = new HashMap<>();
	
	public TabGraphicSqlFetch(DataBaseRepository dataBaseRepository) {
		super(dataBaseRepository);
		tabGraphicSqlBuilders.put(TabGraphicType.detail, new DetailTabGraphicSqlBuilder());
		tabGraphicSqlBuilders.put(TabGraphicType.aggregate, new AggregateTabGraphicSqlBuilder());
	}
	
	@Override
	public void metadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		Database database = dataBaseRepository.findDatabase(dataVolume.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		tabGraphicSqlBuilders.get(graphic.getTabType()).builderSql(sqlBuilder, graphic, dataVolume);
		graphicData.setGraphicSql(sqlBuilder.toSql());
		graphicData.setTabData(dataBaseRepository.querySourceData(dataVolume.getDbName(), sqlBuilder.toSql()));
	}
	
	interface TabGraphicSqlBuilder {
		
		void builderSql(SelectSqlBuilderConverter sqlBuilder, TabGraphic graphic, DataVolume dataVolume);
	}
	
	public abstract class AbstractTabGraphicSqlBuilder implements TabGraphicSqlBuilder {
		
		@Override
		public void builderSql(SelectSqlBuilderConverter sqlBuilder, TabGraphic graphic, DataVolume dataVolume) {
			TabGraphicColumn tabGraphicColumn = graphic.getGraphicColumn();
			for (DimenColumn dimenColumn : tabGraphicColumn.getDimenColumns()) {
				buildDimenColumn(sqlBuilder, dimenColumn, dataVolume);
				sqlBuilder.addOrderBy(dimenColumn.getColumnCode(), dimenColumn.getOrderType());
			}
			for (MeasureColumn measureColumn : tabGraphicColumn.getMeasureColumns()) {
				buildMeasureColumn(sqlBuilder, measureColumn, dataVolume);
			}
			for (FilterColumn filterColumn : tabGraphicColumn.getFilterColumns()) {
				DataColumn dataColumn = dataVolume.findDataColumn(filterColumn.getColumnCode());
				if (dataColumn.getColumnType() == ColumnType.number) {
					sqlBuilder.addNumberCondition(filterColumn.getOperators(), dataColumn.getScreenColumn(), dataColumn.getUnitValue(), filterColumn.getRightValue());
					continue;
				}
				sqlBuilder.addCondition(filterColumn.getOperators(), dataColumn.getColumnType(), dataColumn.getScreenColumn(), filterColumn.getRightValue());
			}
			sqlBuilder.addLimit(graphic.getGraphicPage(), graphic.getGraphicLimit());
		}
		
		protected abstract void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, DimenColumn dimenColumn, DataVolume dataVolume);
		
		protected abstract void buildMeasureColumn(SelectSqlBuilderConverter sqlBuilder, MeasureColumn measureColumn, DataVolume dataVolume);
	}
	
	public class DetailTabGraphicSqlBuilder extends AbstractTabGraphicSqlBuilder {
		
		@Override
		protected void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, DimenColumn dimenColumn, DataVolume dataVolume) {
			if (dimenColumn.getColumnType() == ColumnType.string || dimenColumn.getColumnType() == ColumnType.date) {
				sqlBuilder.addSelectItem(dimenColumn.getColumnCode());
			}
			if (dimenColumn.getColumnType() == ColumnType.number) {
				sqlBuilder.addSelectNumberItem(dimenColumn.getColumnCode(), dimenColumn.getUnitValue());
			}
		}
		
		@Override
		protected void buildMeasureColumn(SelectSqlBuilderConverter sqlBuilder, MeasureColumn measureColumn, DataVolume dataVolume) {
			if (measureColumn.getAggregates() == Aggregates.calculate) {
				sqlBuilder.addSelectExprItem(measureColumn.convertSqlExpr(dataVolume), measureColumn.getColumnCode());
				return;
			}
			sqlBuilder.addSelectNumberItem(measureColumn.getColumnCode(), measureColumn.getUnitValue());
		}
	}
	
	public class AggregateTabGraphicSqlBuilder extends AbstractTabGraphicSqlBuilder {
		
		@Override
		protected void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, DimenColumn dimenColumn, DataVolume dataVolume) {
			if (dimenColumn.getColumnType() == ColumnType.string) {
				sqlBuilder.addSelectItem(dimenColumn.getColumnCode());
				sqlBuilder.addGroupBy(dimenColumn.getColumnCode());
			}
			if (dimenColumn.getColumnType() == ColumnType.number) {
				sqlBuilder.addSelectNumberItem(dimenColumn.getColumnCode(), dimenColumn.getUnitValue());
				sqlBuilder.addGroupBy(dimenColumn.getColumnCode());
			}
			if (dimenColumn.getColumnType() == ColumnType.date) {
				sqlBuilder.addSelectDateCycleItem(dimenColumn.getColumnCode(), dimenColumn.getDateCycle());
				sqlBuilder.addDateCycleGroupBy(dimenColumn.getColumnCode(), dimenColumn.getDateCycle());
			}
		}
		
		@Override
		protected void buildMeasureColumn(SelectSqlBuilderConverter sqlBuilder, MeasureColumn measureColumn, DataVolume dataVolume) {
			if (measureColumn.getAggregates() == Aggregates.calculate) {
				sqlBuilder.addSelectAggregateItem(measureColumn.getAggregates(), measureColumn.convertSqlExpr(dataVolume), measureColumn.getColumnCode());
				return;
			}
			sqlBuilder.addSelectAggregateItem(measureColumn.getAggregates(), measureColumn.getColumnCode(), measureColumn.getUnitValue());
		}
	}
}
