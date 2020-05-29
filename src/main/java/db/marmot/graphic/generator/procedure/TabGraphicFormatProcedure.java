package db.marmot.graphic.generator.procedure;

import com.google.common.collect.Lists;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.graphic.generator.TabGraphicRank;
import db.marmot.volume.DataVolume;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格数据格式化处理以及层级处理
 * @author shaokang
 */
public class TabGraphicFormatProcedure extends GraphicFormatProcedure<TabGraphic, TabGraphicData> {
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		return super.match(graphic, dataVolume) || graphic.getGraphicStyle().isRankColumn();
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		super.processed(graphic, dataVolume, graphicData);
		new TabRankFormatter(graphic, graphicData).format();
	}
	
	public class TabRankFormatter {
		private TabGraphic graphic;
		private TabGraphicData graphicData;
		
		public TabRankFormatter(TabGraphic graphic, TabGraphicData graphicData) {
			this.graphic = graphic;
			this.graphicData = graphicData;
		}
		
		public void format() {
			List<TabGraphicDataColumn> tabGraphicColumns = graphicData.getGraphicDataColumns().stream().filter(TabGraphicDataColumn::isDimenColumn).collect(Collectors.toList());
			if (tabGraphicColumns != null && tabGraphicColumns.size() > 1) {
				List<Map<String, Object>> data = Lists.newArrayList();
				List<TabGraphicRank> tabGraphicRanks = tabGraphicColumns.stream().findFirst().get().getTabGraphicRanks();
				for (int rankRowIndex = 0; rankRowIndex < tabGraphicRanks.size(); rankRowIndex++) {
					TabGraphicRank tabGraphicRank = tabGraphicRanks.get(rankRowIndex);
					Map<String, Object> rankRowData = graphicData.getData().get(tabGraphicRank.getStartRow());
					data.add(buildChildrenData(graphic, tabGraphicRank.getStartRow(), tabGraphicRank.getEndRow(), tabGraphicColumns.size() - 1, 1, rankRowData));
					Map<String, Object> subtotalData = buildSubtotalData(graphic, tabGraphicRank.getEndRow(), 0);
					if (subtotalData != null && !subtotalData.isEmpty()) {
						data.add(subtotalData);
					}
				}
				graphicData.setData(data);
			}
		}
		
		private Map<String, Object> buildChildrenData(TabGraphic graphic, int startRow, int endRow, int rankSize, int rankIndex, Map<String, Object> childrenData) {
			if (rankIndex == rankSize) {
				childrenData.put("children", buildLastRankData(startRow, endRow));
				return childrenData;
			}
			List<Map<String, Object>> data = Lists.newArrayList();
			List<TabGraphicRank> tabGraphicRanks = graphicData.getGraphicDataColumns().get(rankIndex).getTabGraphicRanks();
			for (int rankRowIndex = 0; rankRowIndex < tabGraphicRanks.size(); rankRowIndex++) {
				TabGraphicRank tabGraphicRank = tabGraphicRanks.get(rankRowIndex);
				if ((tabGraphicRank.getEndRow() - tabGraphicRank.getStartRow()) > (endRow - startRow)) {
					childrenData.put("children", buildLastRankData(startRow, endRow));
					return childrenData;
				}
				if (tabGraphicRank.getStartRow() > startRow && tabGraphicRank.getEndRow() <= endRow) {
					Map<String, Object> rankRowData = graphicData.getData().get(tabGraphicRank.getStartRow());
					data.add(buildChildrenData(graphic, tabGraphicRank.getStartRow(), tabGraphicRank.getEndRow(), rankSize, rankIndex + 1, rankRowData));
					Map<String, Object> subtotalData = buildSubtotalData(graphic, tabGraphicRank.getEndRow(), rankIndex);
					if (MapUtils.isNotEmpty(subtotalData)) {
						data.add(subtotalData);
					}
				}
			}
			childrenData.put("children", data);
			return childrenData;
		}
		
		private List<Map<String, Object>> buildLastRankData(int startRow, int endRow) {
			List<Map<String, Object>> data = Lists.newArrayList();
			for (int index = startRow + 1; index <= endRow; index++) {
				data.add(graphicData.getData().get(index));
			}
			return data;
		}
		
		private Map<String, Object> buildSubtotalData(TabGraphic graphic, int rowIndex, int rankIndex) {
			if (rowIndex < graphicData.getData().size() - 1) {
				Map<String, Object> rowData = graphicData.getData().get(rowIndex + 1);
				String columnCode = rowData.keySet().stream().collect(Collectors.toList()).get(rankIndex);
				if (rowData.get(columnCode).toString().equals(graphic.getGraphicStyle().getSubtotalAlias())) {
					return rowData;
				}
			}
			return null;
		}
	}
}
