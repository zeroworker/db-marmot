package db.marmot.graphic.generator;

import com.google.common.collect.Lists;
import db.marmot.converter.ColumnConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.TabGraphicType;
import db.marmot.graphic.TabGraphic;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicData extends GraphicData {
	
	private static final long serialVersionUID = -5288435005429513768L;
	
	/**
	 * 表格显示序号
	 */
	private boolean serialNum;
	
	/**
	 * 表格合并相同列
	 */
	private boolean mergeColumn;
	
	/**
	 * 是否列合计
	 */
	private boolean columnTotal;
	
	/**
	 * 是否行合计
	 */
	private boolean rowTotal;
	
	/**
	 * 表格层级列
	 */
	private boolean rankColumn;
	
	/**
	 * 表格类型
	 */
	private TabGraphicType tabType;

	/**
	 * 表格列 每一个element代表一列数据的字段以及字段样式,每列数据存在多行字段
	 */
	private List<TabGraphicDataColumn> tabColumns = new ArrayList<>();
	
	/**
	 * 表格数据
	 */
	private List<Map<String, Object>> tabData = new ArrayList<>();
	
	/**
	 * 格式化数据
	 */
	public void formatValueTabGraphicData() {
		tabData.forEach(rowData -> {
			for (String columnCode : rowData.keySet()) {
				Object columnValue = rowData.get(columnCode);
				TabGraphicDataColumn tabGraphicColumn = getTabGraphicColumn(columnCode);
				ColumnConverter columnConverter = ConverterAdapter.getInstance().getColumnConverter(tabGraphicColumn.getColumnType());
				//-小计 合计 层级 值填充 等等情况可能导致字段值和字段类型不匹配 若不匹配 不做格式化处理 excel导出时做一样的处理判断
				if (columnConverter.validateColumnValue(columnValue.getClass())) {
					rowData.put(columnCode, columnConverter.formatColumnValue(columnValue, tabGraphicColumn.getDataFormat()));
				}
			}
		});
	}
	
	/**
	 * 格式化成树状结构数据
	 * @param graphic
	 */
	public void formatTreeTabGraphicData(TabGraphic graphic) {
		List<TabGraphicDataColumn> tabGraphicColumns = tabColumns.stream().filter(TabGraphicDataColumn::isDimenColumn).collect(Collectors.toList());
		if (tabGraphicColumns != null && tabGraphicColumns.size() > 1) {
			List<Map<String, Object>> data = Lists.newArrayList();
			List<TabGraphicRank> tabGraphicRanks = tabGraphicColumns.stream().findFirst().get().getTabGraphicRanks();
			for (int rankRowIndex = 0; rankRowIndex < tabGraphicRanks.size(); rankRowIndex++) {
				TabGraphicRank tabGraphicRank = tabGraphicRanks.get(rankRowIndex);
				Map<String, Object> rankRowData = tabData.get(tabGraphicRank.getStartRow());
				data.add(buildChildrenData(graphic, tabGraphicRank.getStartRow(), tabGraphicRank.getEndRow(), tabGraphicColumns.size() - 1, 1, rankRowData));
				Map<String, Object> subtotalData = buildSubtotalData(graphic, tabGraphicRank.getEndRow(), 0);
				if (subtotalData != null && !subtotalData.isEmpty()) {
					data.add(subtotalData);
				}
			}
			this.tabData = data;
		}
	}
	
	private Map<String, Object> buildChildrenData(TabGraphic graphic, int startRow, int endRow, int rankSize, int rankIndex, Map<String, Object> childrenData) {
		if (rankIndex == rankSize) {
			childrenData.put("children", buildLastRankData(startRow, endRow));
			return childrenData;
		}
		List<Map<String, Object>> data = Lists.newArrayList();
		List<TabGraphicRank> tabGraphicRanks = tabColumns.get(rankIndex).getTabGraphicRanks();
		for (int rankRowIndex = 0; rankRowIndex < tabGraphicRanks.size(); rankRowIndex++) {
			TabGraphicRank tabGraphicRank = tabGraphicRanks.get(rankRowIndex);
			if ((tabGraphicRank.getEndRow() - tabGraphicRank.getStartRow()) > (endRow - startRow)) {
				childrenData.put("children", buildLastRankData(startRow, endRow));
				return childrenData;
			}
			if (tabGraphicRank.getStartRow() > startRow && tabGraphicRank.getEndRow() <= endRow) {
				Map<String, Object> rankRowData = tabData.get(tabGraphicRank.getStartRow());
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
			data.add(tabData.get(index));
		}
		return data;
	}
	
	private Map<String, Object> buildSubtotalData(TabGraphic graphic, int rowIndex, int rankIndex) {
		if (rowIndex < tabData.size() - 1) {
			Map<String, Object> rowData = tabData.get(rowIndex + 1);
			String columnCode = rowData.keySet().stream().collect(Collectors.toList()).get(rankIndex);
			if (rowData.get(columnCode).toString().equals(graphic.getGraphicStyle().getSubtotalAlias())) {
				return rowData;
			}
		}
		return null;
	}
	
	private TabGraphicDataColumn getTabGraphicColumn(String columnCode) {
		for (TabGraphicDataColumn tabGraphicColumn : tabColumns) {
			if (tabGraphicColumn.getColumnCode().equals(columnCode)) {
				return tabGraphicColumn;
			}
		}
		throw new GraphicGeneratorException(String.format("数据格式化字段编码%s表格字段不存在", columnCode));
	}
	
	@Override
	public boolean emptyData() {
		return CollectionUtils.isEmpty(tabData);
	}
	
	@Override
	public List<List<String>> buildFileHead() {
		List<List<String>> heads = new ArrayList<>();
		tabColumns.forEach(tabGraphicColumn -> {
			List<String> rowHeads = new ArrayList<>();
			tabGraphicColumn.getRowColumns().forEach(head -> rowHeads.addAll(head.values().stream().collect(Collectors.toList())));
			heads.add(rowHeads);
		});
		return heads;
	}
	
	@Override
	public List<List<Object>> buildFileData() {
		List<List<Object>> data = new ArrayList<>();
		tabData.forEach(rowData -> {
			List rowDataValue = rowData.values().stream().collect(Collectors.toList());
			data.addAll(rowDataValue);
		});
		return data;
	}
	
	public void addTabColumn(TabGraphicDataColumn tabGraphicColumn) {
		tabColumns.add(tabGraphicColumn);
	}
}
