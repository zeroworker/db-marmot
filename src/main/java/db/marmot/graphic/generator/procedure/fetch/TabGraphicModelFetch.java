package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.graphic.DimenColumn;
import db.marmot.graphic.MeasureColumn;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.ColumnVolume;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;

import java.util.*;

/**
 * @author shaokang
 */
public class TabGraphicModelFetch extends GraphicModelFetch<TabGraphic, TabGraphicData> {
	
	private GraphicFetch graphicMockFetch;
	
	public TabGraphicModelFetch(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		super(dataSourceRepository, statisticalGenerateAdapter);
		this.graphicMockFetch = new TabGraphicMockFetch();
	}
	
	@Override
	public void mockMetadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		graphicMockFetch.metadataFetch(graphic, dataVolume, graphicData);
	}
	
	@Override
	public void modelMetadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		List<Map<String, Object>> dimenData = dimenDataFetch(graphic, dataVolume);
		List<Map<String, Object>> tabData = measureDataFetch(graphic, dimenData);
		graphicData.setData(tabData);
	}
	
	/**
	 * 图表维度数据抓取
	 * @param graphic
	 * @return
	 */
	private List<Map<String, Object>> dimenDataFetch(TabGraphic graphic, DataVolume dataVolume) {
		StringBuilder tableBuilder = new StringBuilder();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), null);
		List<DimenColumn> dimenColumns = graphic.getGraphicColumn().getDimenColumns();
		for (int i = 0; i < dimenColumns.size(); i++) {
			DimenColumn dimenColumn = dimenColumns.get(i);
			ColumnVolume columnVolume = dataSourceRepository.findColumnVolume(dataVolume.findDataColumn(dimenColumn.getColumnCode(), dimenColumn.getColumnType()).getScreenColumn());
			SelectSqlBuilderConverter dimenColumnDataSqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), columnVolume.getScript());
			//-添加查询字段
			dimenColumnDataSqlBuilder.addSelectItem(dimenColumn.getColumnCode());
			//-添加过滤条件
			graphic.getGraphicColumn().getFilterColumns().forEach(filterColumn -> {
				for (DataColumn dataColumn : columnVolume.getDataColumns()) {
					if (dataColumn.getColumnCode().equals(filterColumn.getColumnCode())) {
						dimenColumnDataSqlBuilder.addCondition(filterColumn.getOperators(), filterColumn.getColumnType(), dataColumn.getScreenColumn(), filterColumn.getRightValue());
					}
				}
			});
			//-添加排序规则
			dimenColumnDataSqlBuilder.addOrderBy(dimenColumn.getColumnCode(), dimenColumn.getOrderType());
			sqlBuilder.addSelectItem(StringUtils.join("t", i + 1, '.', dimenColumn.getColumnCode()));
			tableBuilder.append("(").append(dimenColumnDataSqlBuilder.toSql()).append(") ").append(StringUtils.join("t", i + 1)).append(",");
		}
		sqlBuilder.addSelectTable(tableBuilder.substring(0, tableBuilder.length() - 1)).addLimit(graphic.getGraphicPage(), graphic.getGraphicLimit());
		return dataSourceRepository.querySourceData(dataVolume.getDbName(), sqlBuilder.toSql());
	}
	
	/**
	 * 图表度量数据抓取
	 * @param graphic
	 * @param dimenData
	 */
	private List<Map<String, Object>> measureDataFetch(TabGraphic graphic, List<Map<String, Object>> dimenData) {
		DimenColumn dateDimenColumn = graphic.getGraphicColumn().findDateDimenColumn();
		//-存在时间维度-时间窗口统计值
		if (dateDimenColumn != null) {
			return timeWindowMeasureDataFetch(graphic, dateDimenColumn, dimenData);
		}
		//-不存在时间维度-聚合统计值
		return timeSumMeasureDataFetch(graphic, dimenData);
	}
	
	/**
	 * 时间窗口度量数据抓取
	 * @param graphic
	 * @param dateDimenColumn
	 * @param dimenData
	 * @return
	 */
	private List<Map<String, Object>> timeWindowMeasureDataFetch(TabGraphic graphic, DimenColumn dateDimenColumn, List<Map<String, Object>> dimenData) {
		
		Map<Date, Map<String, Object>> timeGroupData = new LinkedHashMap<>();
		for (Map<String, Object> dimenRowData : dimenData) {
			timeGroupData.put((Date) dimenRowData.get(dateDimenColumn.getColumnCode()), dimenRowData);
		}
		
		int offset = Integer.valueOf(MVEL.eval(graphic.getGraphicModel().getOffsetExpr()).toString());
		List<Map<String, Object>> aggregateData = statisticalGenerateAdapter.getAggregateData(graphic.getGraphicModel().getModelName(), offset, timeGroupData);
		
		for (int i = 0; i < dimenData.size(); i++) {
			Map<String, Object> aggregateRowData = aggregateData.get(i);
			if (!aggregateRowData.isEmpty()) {
				Map<String, Object> dimenRowData = dimenData.get(i);
				addMeasureData(graphic, dimenRowData, aggregateRowData);
			}
		}
		
		return dimenData;
	}
	
	/**
	 * 时间区间度量合计数据抓起
	 * @param graphic
	 * @param dimenData
	 * @return
	 */
	private List<Map<String, Object>> timeSumMeasureDataFetch(TabGraphic graphic, List<Map<String, Object>> dimenData) {
		List<Date> timeValue = new ArrayList<>();
		graphic.getGraphicColumn()
				.getFilterColumns()
				.stream()
				.filter(filterColumn -> filterColumn.getColumnType() == ColumnType.date)
				.forEach(filterColumn -> timeValue.add((Date)filterColumn.getRightValue()));
		timeValue.stream().sorted(Date::compareTo).sorted(Comparator.reverseOrder());

		int offset = Integer.valueOf(MVEL.eval(graphic.getGraphicModel().getModelName()).toString());
		//-维度数据循环,补全维度数据统计值
		for (Map<String, Object> dimenRowData : dimenData) {
			//获取所有模型维度行度量数据
			Map<String, Object> aggregateRowData = statisticalGenerateAdapter.getAggregateData(graphic.getGraphicModel().getModelName(), offset, timeValue.get(1), timeValue.get(0), dimenRowData);
			addMeasureData(graphic, dimenRowData, aggregateRowData);
		}
		return dimenData;
	}
	
	private void addMeasureData(TabGraphic graphic, Map<String, Object> dimenRowData, Map<String, Object> aggregateRowData) {//-创建一行图表数据
		for (MeasureColumn measureColumn : graphic.getGraphicColumn().getMeasureColumns()) {
			//-度量字段聚合计算处理
			if (measureColumn.getAggregates() != Aggregates.calculate) {
				dimenRowData.put(measureColumn.getColumnCode(), aggregateRowData.get(measureColumn.getColumnCode()));
				continue;
			}
			dimenRowData.put(measureColumn.getColumnCode(), MVEL.eval(measureColumn.getCalExpr(), aggregateRowData));
		}
	}
}
