package db.marmot.graphic.generator.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.enums.TabGraphicType;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.volume.DataVolume;

import lombok.Getter;
import lombok.Setter;

/**
 * 表格数据层级处理-针对维度字段做层级处理
 * @author shaokang
 */
public class TabGraphicRankProcedure implements GraphicProcedure<TabGraphic, TabGraphicData> {
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		return graphic.getGraphicStyle().isRankColumn() && graphic.getTabType() == TabGraphicType.aggregate;
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		GraphicRank graphicRank = new TabGraphicRank(graphic, graphicData);
		//-1,构建层级图表数据
		graphicRank.tabGraphicRankBuild();
		//-2.添加层级数据
		graphicRank.addTabGraphicRank();
	}
	
	@Override
	public int getOrder() {
		return 7;
	}
	
	public interface GraphicRank {
		
		/**
		 * 层级数据构建
		 */
		void tabGraphicRankBuild();
		
		/**
		 * 添加图表层级数据
		 */
		void addTabGraphicRank();
		
	}
	
	public class TabGraphicRank implements GraphicRank {
		
		/**
		 * 表格数据行角标
		 */
		private int rowIndex = 0;
		
		/**
		 * 表格图表
		 */
		private TabGraphic graphic;
		
		/**
		 * 表格图表数据
		 */
		private TabGraphicData graphicData;
		
		/**
		 * 维度表格字段
		 */
		private List<TabGraphicDataColumn> dimenTabColumns;
		
		/**
		 * 层级图表数据
		 */
		private List<Map<String, Object>> ranKTabData = Lists.newArrayList();
		
		/**
		 * 字段层级行
		 */
		private Map<String, List<ColumnRankRow>> columnRanks = Maps.newLinkedHashMap();
		
		public TabGraphicRank(TabGraphic graphic, TabGraphicData graphicData) {
			this.graphic = graphic;
			this.graphicData = graphicData;
			this.dimenTabColumns = graphicData.getTabColumns().stream().filter(TabGraphicDataColumn::isDimenColumn).collect(Collectors.toList());
		}
		
		@Override
		public void tabGraphicRankBuild() {
			//-1.循环表格数据
			for (Map<String, Object> rowData : graphicData.getTabData()) {
				//-2.只有一个维度字时不做层级;最后一个维度字段不做层级
				for (int dimenIndex = 0; dimenIndex < dimenTabColumns.size() - 1; dimenIndex++) {
					TabGraphicDataColumn dimenColumn = dimenTabColumns.get(dimenIndex);
					Object value = rowData.get(dimenColumn.getColumnCode());
					//-3.排除小计行数据 不添加层级行
					if (!value.toString().equals(graphic.getGraphicStyle().getSubtotalAlias()) && !value.toString().equals("")) {
						List<ColumnRankRow> columnRankRows = getColumnRankRows(dimenColumn.getColumnCode());
						//-首次创建列层级行计数
						if (columnRankRows.isEmpty()) {
							rowIndex++;
							//-创建层级行计数
							columnRankRows.add(createColumnRankRow(value));
							//-创建层级数据
							createColumnRankRowData(dimenColumn.getColumnCode(), rowData);
							continue;
						}
						//-获取最后一个层级行计数,层级行计数的数据值和当前列数据值不一致创建层级计数
						ColumnRankRow columnRankRow = columnRankRows.get(columnRankRows.size() - 1);
						if (!columnRankRow.getValue().equals(value)) {
							rowIndex++;
							//-创建层级行计数
							columnRankRows.add(createColumnRankRow(value));
							//-创建层级数据
							createColumnRankRowData(dimenColumn.getColumnCode(), rowData);
							continue;
						}
						//-更新层级行计数结束行
						columnRankRow.setEndRow(rowIndex);
					}
				}
				rowIndex++;
				paddingDimenRankRowData(rowData);
			}
		}
		
		/**
		 * 获取字段层级行计数
		 * @param columnCode
		 * @return
		 */
		private List<ColumnRankRow> getColumnRankRows(String columnCode) {
			List<ColumnRankRow> columnRankRows = columnRanks.get(columnCode);
			if (columnRankRows == null) {
				columnRankRows = new ArrayList<>();
				columnRanks.put(columnCode, columnRankRows);
			}
			return columnRankRows;
		}
		
		/**
		 * 创建层级行
		 * @param value
		 * @return
		 */
		private ColumnRankRow createColumnRankRow(Object value) {
			ColumnRankRow columnRankRow = new ColumnRankRow(value);
			columnRankRow.setStartRow(rowIndex);
			columnRankRow.setEndRow(rowIndex);
			return columnRankRow;
		}
		
		/**
		 * 创建维度字段层级行数据
		 * @param dimenCode
		 * @param rowData
		 */
		private void createColumnRankRowData(String dimenCode, Map<String, Object> rowData) {
			Map<String, Object> rankRowData = Maps.newLinkedHashMap();
			//-创建层级行数据,根据行数据字段数将非层级列字段设置为空串
			rowData.keySet().forEach(columnCode -> {
				if (columnCode.equals(dimenCode)) {
					rankRowData.put(dimenCode, rowData.get(dimenCode));
					return;
				}
				rankRowData.put(columnCode, "");
			});
			ranKTabData.add(rankRowData);
		}
		
		/**
		 * 将行数据维度列值填充为空串
		 * @param rowData
		 */
		private void paddingDimenRankRowData(Map<String, Object> rowData) {
			//-1.若行数据为小计行,将非小计值的维度列值替换为空串
			if (rowData.values().contains(graphic.getGraphicStyle().getSubtotalAlias())) {
				dimenTabColumns.forEach(dimenColumn -> {
					if (!rowData.get(dimenColumn.getColumnCode()).toString().equals(graphic.getGraphicStyle().getSubtotalAlias())) {
						rowData.put(dimenColumn.getColumnCode(), "");
					}
				});
				ranKTabData.add(rowData);
				return;
			}
			//-2. 行数据维度列值填充为空串
			dimenTabColumns.forEach(dimenColumn -> rowData.put(dimenColumn.getColumnCode(), ""));
			ranKTabData.add(rowData);
		}
		
		@Override
		public void addTabGraphicRank() {
			
			//-1.将层级数据添加到图表数据中
			graphicData.setTabData(ranKTabData);
			
			//-2.添加维度字段层级行
			for (int dimenIndex = 0; dimenIndex < dimenTabColumns.size() - 1; dimenIndex++) {
				TabGraphicDataColumn graphicColumn = dimenTabColumns.get(dimenIndex);
				List<ColumnRankRow> columnRankRows = columnRanks.get(graphicColumn.getColumnCode());
				columnRankRows.forEach(columnRankRow -> graphicColumn.addTabGraphicRank(columnRankRow.getStartRow(), columnRankRow.getEndRow()));
			}
		}
		
		@Setter
		@Getter
		class ColumnRankRow {
			private Object value;
			private int startRow;
			private int endRow;
			
			public ColumnRankRow(Object value) {
				this.value = value;
			}
		}
	}
}
