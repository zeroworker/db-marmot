package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.enums.GraphicCycle;
import db.marmot.enums.GraphicLayout;
import db.marmot.graphic.*;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataBaseRepository;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shaokang
 */
public abstract class GraphicSqlFetch<G extends Graphic, D extends GraphicData> implements GraphicFetch<G, D> {
	
	protected DataBaseRepository dataBaseRepository;
	private Map<GraphicLayout, GraphicSqlBuilder> graphicSqlBuilders = new HashMap<>();
	protected ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public GraphicSqlFetch(DataBaseRepository dataBaseRepository) {
		this.dataBaseRepository = dataBaseRepository;
		graphicSqlBuilders.put(GraphicLayout.detail, new DetailGraphicSqlBuilder());
		graphicSqlBuilders.put(GraphicLayout.aggregate, new AggregateGraphicSqlBuilder());
	}
	
	@Override
	public void metadataFetch(G graphic, DataVolume dataVolume, D graphicData) {
		Database database = dataBaseRepository.findDatabase(dataVolume.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		graphicSqlBuilders.get(graphic.getGraphicLayout()).builderSql(sqlBuilder, graphic, dataVolume);
		graphicData.setGraphicSql(sqlBuilder.toSql());
		graphicData.setData(dataBaseRepository.querySourceData(dataVolume.getDbName(), sqlBuilder.toSql()));
	}
	
	interface GraphicSqlBuilder {
		
		void builderSql(SelectSqlBuilderConverter sqlBuilder, Graphic graphic, DataVolume dataVolume);
	}
	
	public abstract class AbstractGraphicSqlBuilder implements GraphicSqlBuilder {
		
		@Override
		public void builderSql(SelectSqlBuilderConverter sqlBuilder, Graphic graphic, DataVolume dataVolume) {
			GraphicColumn graphicColumn = graphic.getGraphicColumn();
			for (DimenColumn dimenColumn : graphicColumn.getDimenColumns()) {
				buildDimenColumn(sqlBuilder, graphic.getGraphicCycle(), dimenColumn, dataVolume);
				sqlBuilder.addOrderBy(dimenColumn.getColumnCode(), dimenColumn.getOrderType());
			}
			for (MeasureColumn measureColumn : graphicColumn.getMeasureColumns()) {
				buildMeasureColumn(sqlBuilder, measureColumn, dataVolume);
			}
			for (FilterColumn filterColumn : graphicColumn.getFilterColumns()) {
				DataColumn dataColumn = dataVolume.findDataColumn(filterColumn.getColumnCode(), filterColumn.getColumnType());
				if (dataColumn.getColumnType() == ColumnType.number) {
					sqlBuilder.addNumberCondition(filterColumn.getOperators(), dataColumn.getScreenColumn(), dataColumn.getUnitValue(), filterColumn.getRightValue());
					continue;
				}
				sqlBuilder.addCondition(filterColumn.getOperators(), dataColumn.getColumnType(), dataColumn.getScreenColumn(), filterColumn.getRightValue());
			}
			sqlBuilder.addLimit(graphic.getGraphicPage(), graphic.getGraphicLimit());
		}
		
		protected abstract void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, GraphicCycle graphicCycle, DimenColumn dimenColumn, DataVolume dataVolume);
		
		protected abstract void buildMeasureColumn(SelectSqlBuilderConverter sqlBuilder, MeasureColumn measureColumn, DataVolume dataVolume);
	}
	
	public class DetailGraphicSqlBuilder extends AbstractGraphicSqlBuilder {
		
		@Override
		protected void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, GraphicCycle graphicCycle, DimenColumn dimenColumn, DataVolume dataVolume) {
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
	
	public class AggregateGraphicSqlBuilder extends AbstractGraphicSqlBuilder {
		
		@Override
		protected void buildDimenColumn(SelectSqlBuilderConverter sqlBuilder, GraphicCycle graphicCycle, DimenColumn dimenColumn, DataVolume dataVolume) {
			if (dimenColumn.getColumnType() == ColumnType.string) {
				sqlBuilder.addSelectItem(dimenColumn.getColumnCode());
				sqlBuilder.addGroupBy(dimenColumn.getColumnCode());
			}
			if (dimenColumn.getColumnType() == ColumnType.number) {
				sqlBuilder.addSelectNumberItem(dimenColumn.getColumnCode(), dimenColumn.getUnitValue());
				sqlBuilder.addGroupBy(dimenColumn.getColumnCode());
			}
			if (dimenColumn.getColumnType() == ColumnType.date && graphicCycle != GraphicCycle.non) {
				sqlBuilder.addSelectDateCycleItem(dimenColumn.getColumnCode(), graphicCycle);
				sqlBuilder.addDateCycleGroupBy(dimenColumn.getColumnCode(), graphicCycle);
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
