package db.marmot.graphic.generator.procedure;

import com.google.common.collect.Maps;
import db.marmot.converter.ConverterAdapter;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.converter.TotalConverter;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.volume.DataVolume;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据表合计处理
 * @author shaokang
 */
public class TabGraphicTotalProcedure implements GraphicProcedure<TabGraphic, TabGraphicData> {
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		return graphic.getGraphicStyle().isColumnTotal();
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		
		//-1.获取所有需要列合计度量字段
		List<TabGraphicDataColumn> measureTabColumns = graphicData.getGraphicDataColumns().stream().filter(TabGraphicDataColumn::isColumnTotal).collect(Collectors.toList());
		
		//-2.遍历表格数据做合计处理
		GraphicTotal graphicTotal = new TabGraphicTotal(measureTabColumns);
		graphicData.getData().forEach(rowData -> graphicTotal.columnTotalBuild(rowData));
		
		//-3.添加合计数据到表格数据中
		Map<String, Object> columnTotalData = Maps.newLinkedHashMap();
		graphicData.getGraphicDataColumns().forEach(tabGraphicColumn -> {
			if (tabGraphicColumn.isColumnTotal()) {
				List<BigDecimal> columnTotalValues = graphicTotal.getColumnTotalData(tabGraphicColumn.getColumnCode());
				TotalConverter totalConverter = ConverterAdapter.getInstance().getTotalConverter(tabGraphicColumn.getColumnTotalType());
				columnTotalData.put(tabGraphicColumn.getColumnCode(), totalConverter.calculateTotalValue(columnTotalValues));
				return;
			}
			columnTotalData.put(tabGraphicColumn.getColumnCode(), "");
		});
		graphicData.getData().add(columnTotalData);
	}
	
	@Override
	public int getOrder() {
		return 6;
	}
	
	public interface GraphicTotal {
		
		/**
		 * 列合计数据创建
		 */
		void columnTotalBuild(Map<String, Object> rowData);
		
		/**
		 * 获取列合计数据
		 * @return
		 */
		List<BigDecimal> getColumnTotalData(String columnCode);
	}
	
	public class TabGraphicTotal implements GraphicTotal {
		
		/**
		 * 列合计度量表格字段
		 */
		private List<TabGraphicDataColumn> measureTabColumns;
		
		/**
		 * 列合计数据
		 */
		private Map<String, List<BigDecimal>> columnTotalData = Maps.newLinkedHashMap();
		
		public TabGraphicTotal(List<TabGraphicDataColumn> measureTabColumns) {
			this.measureTabColumns = measureTabColumns;
		}
		
		@Override
		public void columnTotalBuild(Map<String, Object> rowData) {
			measureTabColumns.forEach(tabGraphicColumn -> {
				List<BigDecimal> columnTotalValues = columnTotalData.get(tabGraphicColumn.getColumnCode());
				if (columnTotalValues == null) {
					columnTotalValues = new ArrayList<>();
					columnTotalData.put(tabGraphicColumn.getColumnCode(), columnTotalValues);
				}
				columnTotalValues.add((BigDecimal) rowData.get(tabGraphicColumn.getColumnCode()));
			});
		}
		
		@Override
		public List<BigDecimal> getColumnTotalData(String columnCode) {
			return columnTotalData.get(columnCode);
		}
	}
}
