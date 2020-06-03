package db.marmot.graphic.generator.procedure.fetch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.graphic.DimenColumn;
import db.marmot.graphic.MeasureColumn;
import db.marmot.graphic.TabColumnStyle;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class TabGraphicMockFetch extends GraphicMockFetch<TabGraphic, TabGraphicData> {
	
	@Override
	public void metadataFetch(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		List<Map<String, Object>> metadata = Lists.newArrayList();
		
		//-mock 三组维度数据
		for (int i = 0; i < 3; i++) {
			metadata.addAll(mockDimenColumnData(graphic, dataVolume, i + 1));
		}
		
		//-mock度量数据
		metadata.forEach(rowData -> mockMeasureColumnData(rowData, graphic));
		
		//-添加到图表数据中
		graphicData.setData(metadata);
	}
	
	/**
	 * mock 维度数据
	 * @param graphic
	 * @param dataVolume
	 * @param set
	 * @return
	 */
	private List<Map<String, Object>> mockDimenColumnData(TabGraphic graphic, DataVolume dataVolume, int set) {
		List<Map<String, Object>> setMetadata = Lists.newArrayList();
		int rows = graphic.getGraphicColumn().getDimenColumns().size();
		if (rows == 1) {
			Map<String, Object> rowData = Maps.newLinkedHashMap();
			DimenColumn dimenColumn = graphic.getGraphicColumn().getDimenColumns().stream().findFirst().get();
			Object value = mockColumnValue(graphic, dataVolume, dimenColumn.getColumnCode(), dimenColumn.getColumnType());
			if (dimenColumn.getColumnType() == ColumnType.date) {
				value = mockDateColumnValue((Date) value, -(set - 1));
			} else {
				value = StringUtils.join(value, "_", set);
			}
			rowData.put(dimenColumn.getColumnCode(), value);
			setMetadata.add(rowData);
			return setMetadata;
		}
		for (int row = 0; row < rows; row++) {
			setMetadata.add(mockRowDimenColumnData(graphic, dataVolume, row + 1, set));
		}
		return setMetadata;
	}
	
	/**
	 * mock 一行维度数据
	 * @param graphic
	 * @param dataVolume
	 * @param row
	 * @param set
	 * @return
	 */
	private Map<String, Object> mockRowDimenColumnData(TabGraphic graphic, DataVolume dataVolume, int row, int set) {
		Map<String, Object> rowData = Maps.newLinkedHashMap();
		List<DimenColumn> dimenColumns = graphic.getGraphicColumn().getDimenColumns();
		for (int i = 0; i < dimenColumns.size(); i++) {
			mockRowDimenColumnData(graphic, dataVolume, dimenColumns.get(i), rowData, dimenColumns.size(), i + 1, row, set);
		}
		return rowData;
	}
	
	/**
	 * mock 一行维度数据
	 * @param graphic
	 * @param dataVolume
	 * @param dimenColumn
	 * @param rowData
	 * @param columnCount
	 * @param columnIndex
	 * @param row
	 * @param set
	 */
	private void mockRowDimenColumnData(TabGraphic graphic, DataVolume dataVolume, DimenColumn dimenColumn, Map<String, Object> rowData, int columnCount, int columnIndex, int row, int set) {
		Object value = mockColumnValue(graphic, dataVolume, dimenColumn.getColumnCode(), dimenColumn.getColumnType());
		if (columnCount == columnIndex) {
			if (dimenColumn.getColumnType() == ColumnType.date) {
				value = mockDateColumnValue((Date) value, -(row - 1));
			} else {
				value = StringUtils.join(value, "_", row);
			}
		} else {
			if (columnCount == columnIndex + 1) {
				if (dimenColumn.getColumnType() == ColumnType.date) {
					value = mockDateColumnValue((Date) value, -(set - 1));
				} else {
					value = StringUtils.join(value, "_", set);
				}
			}
		}
		rowData.put(dimenColumn.getColumnCode(), value);
	}
	
	/**
	 * mock 度量数据
	 * @param rowData
	 * @param graphic
	 */
	private void mockMeasureColumnData(Map<String, Object> rowData, TabGraphic graphic) {
		List<MeasureColumn> measureColumns = graphic.getGraphicColumn().getMeasureColumns();
		
		//-mock包含计算字段在内所有度量字段的值 维度字段当前只支持数字 顾取0-100随机数
		Map<String, Object> measureColumnsValues = new HashMap<>();
		measureColumns.forEach(measureColumn -> {
			//-度量字段聚合计算处理
			if (measureColumn.getAggregates() == Aggregates.calculate) {
				Map<String, Aggregates> aggregateVariables = measureColumn.parseExprVariable();
				aggregateVariables.keySet().forEach(variableColumn -> measureColumnsValues.put(variableColumn, new BigDecimal(String.valueOf(Math.random() * 100))));
				return;
			}
			measureColumnsValues.put(measureColumn.getColumnCode(), new BigDecimal(String.valueOf(Math.random() * 100)));
		});
		
		//-根据度量字段添加一行数据的度量值
		measureColumns.forEach(measureColumn -> {
			//-度量字段聚合计算处理
			if (measureColumn.getAggregates() == Aggregates.calculate) {
				rowData.put(measureColumn.getColumnCode(), MVEL.eval(measureColumn.getCalExpr(), measureColumnsValues));
				return;
			}
			rowData.put(measureColumn.getColumnCode(), measureColumnsValues.get(measureColumn.getColumnCode()));
		});
	}
	
	/**
	 * mock 字段值
	 * @param graphic
	 * @param dataVolume
	 * @param columnCode
	 * @param columnType
	 * @return
	 */
	private Object mockColumnValue(TabGraphic graphic, DataVolume dataVolume, String columnCode, ColumnType columnType) {
		
		if (columnType == ColumnType.string) {
			List<TabColumnStyle> tabColumnStyles = graphic.getGraphicStyle().getTabColumnStyles();
			for (TabColumnStyle tabColumnStyle : tabColumnStyles) {
				if (tabColumnStyle.getColumnCode().equals(columnCode)) {
					return tabColumnStyle.getColumnName();
				}
			}
			for (DataColumn dataColumn : dataVolume.getDataColumns()) {
				if (dataColumn.getColumnCode().equals(columnCode)) {
					return dataColumn.getColumnName();
				}
			}
		}
		
		if (columnType == ColumnType.number) {
			return new BigDecimal(new Double(Math.random() * 100));
		}
		
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * mock时间字段值 按天mock
	 * @param date
	 * @param day
	 * @return
	 */
	private Date mockDateColumnValue(Date date, int day) {
		Period p = Period.ofDays(day);
		LocalDate now = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return Date.from(now.plus(p).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
}
