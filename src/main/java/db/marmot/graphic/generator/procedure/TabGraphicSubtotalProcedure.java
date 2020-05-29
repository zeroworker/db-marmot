package db.marmot.graphic.generator.procedure;

import com.google.common.collect.Maps;
import db.marmot.enums.GraphicLayout;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据列小计处理(针对聚合统计表格数据) 列小计做整体列小记不做指定字段的列小计 感觉没撒意义
 * @author shaokang
 */
public class TabGraphicSubtotalProcedure implements GraphicProcedure<TabGraphic, TabGraphicData> {
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		return graphic.getGraphicLayout() == GraphicLayout.aggregate && graphic.getGraphicStyle().isColumnSubtotal();
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		
		//-1.拆分表格图表字段拆分为维度字段和度量字段
		List<TabGraphicDataColumn> dimenTabColumns = graphicData.getGraphicDataColumns().stream().filter(TabGraphicDataColumn::isDimenColumn).collect(Collectors.toList());
		List<TabGraphicDataColumn> measureTabColumns = graphicData.getGraphicDataColumns().stream().filter(tabGraphicColumn -> !tabGraphicColumn.isDimenColumn()).collect(Collectors.toList());
		
		//-2.列小计按列处理
		for (int columnIndex = 0; columnIndex < dimenTabColumns.size(); columnIndex++) {
			TabGraphicDataColumn tabGraphicColumn = graphicData.getGraphicDataColumns().get(columnIndex);
			if (tabGraphicColumn.isColumnSubtotal()) {
				GraphicSubtotal graphicSubtotal = new TabGraphicSubtotal(columnIndex, graphic, dimenTabColumns.get(columnIndex), dimenTabColumns, measureTabColumns);
				for (int rowIndex = 0; rowIndex < graphicData.getData().size(); rowIndex++) {
					//-2.1 该列对应的行数据计算该列的小计
					graphicSubtotal.setRowIndex(rowIndex).columnSubtotalBuild(graphicData.getData().get(rowIndex));
				}
				//-2.2 添加该列小计数据
				graphicSubtotal.addColumnSubtotalData(graphicData.getData());
			}
		}
	}
	
	@Override
	public int getOrder() {
		return 5;
	}
	
	public interface GraphicSubtotal {
		
		/**
		 * 设置数据行角标
		 * @param rowIndex
		 */
		GraphicSubtotal setRowIndex(int rowIndex);
		
		/**
		 * 小计数据构建
		 * @param rowData
		 * @return
		 */
		void columnSubtotalBuild(Map<String, Object> rowData);
		
		/**
		 * 添加小计数据
		 * @param tabData
		 */
		void addColumnSubtotalData(List<Map<String, Object>> tabData);
	}
	
	public class TabGraphicSubtotal implements GraphicSubtotal {
		
		/**
		 * 数据行角标
		 */
		private int rowIndex;
		
		/**
		 * 字段列角标
		 */
		private int columnIndex;
		
		/**
		 * 图表
		 */
		private TabGraphic graphic;
		
		/**
		 * 图表字段
		 */
		private TabGraphicDataColumn subtotalColumn;
		
		/**
		 * 维度表格字段
		 */
		private List<TabGraphicDataColumn> dimenTabColumns;
		
		/**
		 * 度量表格字段
		 */
		private List<TabGraphicDataColumn> measureTabColumns;
		
		/**
		 * 列小计数据
		 */
		private List<ColumnSubtotalData> columnSubtotals = new ArrayList<>();
		
		public TabGraphicSubtotal(int columnIndex, TabGraphic graphic, TabGraphicDataColumn subtotalColumn, List<TabGraphicDataColumn> dimenTabColumns, List<TabGraphicDataColumn> measureTabColumns) {
			this.columnIndex = columnIndex;
			this.graphic = graphic;
			this.subtotalColumn = subtotalColumn;
			this.dimenTabColumns = dimenTabColumns;
			this.measureTabColumns = measureTabColumns;
		}
		
		@Override
		public GraphicSubtotal setRowIndex(int rowIndex) {
			this.rowIndex = rowIndex;
			return this;
		}
		
		@Override
		public void columnSubtotalBuild(Map<String, Object> rowData) {
			if (!rowData.values().contains(graphic.getGraphicStyle().getSubtotalAlias())) {
				int rowSubtotalIndex = subtotalIndex(rowData);
				if (rowSubtotalIndex != -1) {
					Map<String, Object> subtotal = indexOfSubtotal(rowData, rowSubtotalIndex);
					for (TabGraphicDataColumn tabGraphicColumn : measureTabColumns) {
						BigDecimal rowValue = (BigDecimal) rowData.get(tabGraphicColumn.getColumnCode());
						BigDecimal subtotalValue = (BigDecimal) subtotal.get(tabGraphicColumn.getColumnCode());
						subtotal.put(tabGraphicColumn.getColumnCode(), rowValue.add(subtotalValue));
					}
				}
			}
		}
		
		/**
		 * 计算小计数据角标位
		 * @param rowData
		 * @return
		 */
		private int subtotalIndex(Map<String, Object> rowData) {
			String columnCode = subtotalColumn.getColumnCode();
			ColumnSubtotalData columnSubtotalData = CollectionUtils.isNotEmpty(columnSubtotals) ? columnSubtotals.get(columnSubtotals.size() - 1) : null;
			if (columnIndex + 1 == dimenTabColumns.size()) {
				if (columnSubtotalData == null || columnSubtotalData.getColumnValue().equals(rowData.get(columnCode))) {
					columnSubtotalData = new ColumnSubtotalData(rowIndex, columnCode, rowData.get(columnCode));
					columnSubtotalData.addSubtotalData(createSubtotalData(rowData));
					columnSubtotals.add(columnSubtotalData);
					return -1;
				}
				return columnSubtotals.size() - 1;
			}
			if (columnSubtotalData == null || !columnSubtotalData.getColumnValue().equals(rowData.get(columnCode))) {
				columnSubtotalData = new ColumnSubtotalData(rowIndex, columnCode, rowData.get(columnCode));
				columnSubtotalData.addSubtotalData(createSubtotalData(rowData));
				columnSubtotals.add(columnSubtotalData);
				return -1;
			}
			return columnSubtotals.size() - 1;
		}
		
		/**
		 * 创建小计算数据
		 * @param rowData
		 * @return
		 */
		private Map<String, Object> createSubtotalData(Map<String, Object> rowData) {
			Map<String, Object> subtotal = Maps.newLinkedHashMap();
			rowData.forEach((s, o) -> subtotal.put(s, o));
			subtotal.put(subtotalColumn.getColumnCode(), graphic.getGraphicStyle().getColumnTotalAlias());
			if (graphic.getGraphicStyle().isMergeColumn() || graphic.getGraphicStyle().isRankColumn()) {
				for (int index = columnIndex + 1; index < dimenTabColumns.size(); index++) {
					TabGraphicDataColumn tabGraphicColumn = dimenTabColumns.get(index);
					subtotal.put(tabGraphicColumn.getColumnCode(), "");
				}
				return subtotal;
			}
			for (TabGraphicDataColumn tabGraphicColumn : dimenTabColumns) {
				if (!tabGraphicColumn.getColumnCode().equals(subtotalColumn.getColumnCode())) {
					subtotal.put(tabGraphicColumn.getColumnCode(), "");
				}
			}
			return subtotal;
		}
		
		/**
		 * 获取小计数据
		 * @param rowData
		 * @param rowSubtotalIndex
		 * @return
		 */
		private Map<String, Object> indexOfSubtotal(Map<String, Object> rowData, int rowSubtotalIndex) {
			ColumnSubtotalData columnSubtotalData = columnSubtotals.get(rowSubtotalIndex);
			columnSubtotalData.setRowIndex(rowIndex);
			for (TabGraphicDataColumn tabGraphicColumn : dimenTabColumns) {
				if (!tabGraphicColumn.getColumnCode().equals(subtotalColumn.getColumnCode())) {
					if (!columnSubtotalData.getSubtotalValue(tabGraphicColumn.getColumnCode()).equals("")) {
						columnSubtotalData.addSubtotalData(tabGraphicColumn.getColumnCode(), rowData.get(tabGraphicColumn.getColumnCode()));
					}
				}
			}
			return columnSubtotalData.getSubtotalData();
		}
		
		@Override
		public void addColumnSubtotalData(List<Map<String, Object>> tabData) {
			for (int index = 0; index < columnSubtotals.size(); index++) {
				ColumnSubtotalData columnSubtotalData = columnSubtotals.get(index);
				tabData.add(columnSubtotalData.getRowIndex() + index + 1, columnSubtotalData.getSubtotalData());
			}
		}
		
		@Setter
		@Getter
		class ColumnSubtotalData {
			/**
			 * 数据角标
			 */
			private int rowIndex;
			
			/**
			 * 小计字段编码
			 */
			private String columnCode;
			
			/**
			 * 小计字段值
			 */
			private Object columnValue;
			
			/**
			 * 小计数据
			 */
			private Map<String, Object> subtotalData;
			
			public ColumnSubtotalData(int rowIndex, String columnCode, Object columnValue) {
				this.rowIndex = rowIndex;
				this.columnCode = columnCode;
				this.columnValue = columnValue;
			}
			
			public ColumnSubtotalData addSubtotalData(Map<String, Object> subtotalData) {
				this.subtotalData = subtotalData;
				return this;
			}
			
			public Object getSubtotalValue(String columnCode) {
				return subtotalData.get(columnCode);
			}
			
			public void addSubtotalData(String columnCode, Object value) {
				subtotalData.put(columnCode, value);
			}
		}
	}
}
