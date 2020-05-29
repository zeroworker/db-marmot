package db.marmot.graphic.download;

import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.enums.GraphicType;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.graphic.generator.TabGraphicRank;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author shaokang
 */
public class TabGraphicDownloader extends GraphicDataDownloader<TabGraphicData> {
	
	public TabGraphicDownloader(GraphicGeneratorAdapter graphicGeneratorFactory) {
		super(graphicGeneratorFactory);
	}
	
	@Override
	public void registerWriteHandler(TabGraphicData graphicData, List<WriteHandler> writeHandlers) {
		writeHandlers.add(new ExcelGroupRankStrategy(graphicData));
		writeHandlers.add(new ExcelFreezeSheetStyleStrategy(graphicData));
	}
	
	@Override
	public GraphicType graphicType() {
		return GraphicType.cross_tab;
	}
	
	class ExcelFreezeSheetStyleStrategy implements SheetWriteHandler {
		
		private TabGraphicData graphicData;
		
		public ExcelFreezeSheetStyleStrategy(TabGraphicData graphicData) {
			this.graphicData = graphicData;
		}
		
		@Override
		public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
		}
		
		@Override
		public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
			int freezeColumnNum = 0;
			for (TabGraphicDataColumn tabGraphicColumn : graphicData.getGraphicDataColumns()) {
				if (tabGraphicColumn.isFreezeColumn()) {
					freezeColumnNum++;
				}
			}
			int freezeRowNum = graphicData.getGraphicDataColumns().stream().findFirst().get().getRowColumns().size();
			writeSheetHolder.getSheet().createFreezePane(freezeColumnNum, freezeRowNum, freezeColumnNum, freezeRowNum);
		}
	}
	
	class ExcelGroupRankStrategy implements CellWriteHandler {
		
		private TabGraphicData graphicData;
		
		private Map<Integer, List<ColumnMerge>> columnMergeRow = Maps.newHashMap();
		
		public ExcelGroupRankStrategy(TabGraphicData graphicData) {
			this.graphicData = graphicData;
		}
		
		@Override
		public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
		}
		
		@Override
		public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
		}
		
		@Override
		public void afterCellDispose(	WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex,
										Boolean isHead) {
			if (!isHead) {
				merge(writeSheetHolder.getSheet(), cell);
			}
			if (lastCell(cell)) {
				groupRow(writeSheetHolder.getSheet());
			}
		}
		
		private void groupRow(Sheet sheet) {
			int rowNum = graphicData.getGraphicDataColumns().stream().findFirst().get().getRowColumns().size();
			if (graphicData.isRankColumn()) {
				for (TabGraphicDataColumn tabGraphicColumn : graphicData.getGraphicDataColumns()) {
					if (CollectionUtils.isNotEmpty(tabGraphicColumn.getTabGraphicRanks())) {
						for (TabGraphicRank tabGraphicRank : tabGraphicColumn.getTabGraphicRanks()) {
							sheet.groupRow((rowNum + 1) + tabGraphicRank.getStartRow(), rowNum + tabGraphicRank.getEndRow());
						}
					}
				}
			}
		}
		
		private void merge(Sheet sheet, Cell cell) {
			if (graphicData.isMergeColumn()) {
				TabGraphicDataColumn tabGraphicColumn = graphicData.getGraphicDataColumns().get(cell.getColumnIndex());
				if (tabGraphicColumn.isMergeColumn()) {
					String value = cell.getStringCellValue();
					if (!value.equals("")) {
						List<ColumnMerge> columnMerges = columnMergeRow.get(cell.getColumnIndex());
						if (columnMerges == null) {
							columnMerges = Lists.newArrayList();
							columnMergeRow.put(cell.getColumnIndex(), columnMerges);
						}
						ColumnMerge columnMerge = CollectionUtils.isNotEmpty(columnMerges) ? columnMerges.get(columnMerges.size() - 1) : null;
						if (columnMerge == null || !columnMerge.getValue().equals(value)) {
							columnMerges.add(new ColumnMerge(value, cell.getRowIndex(), cell.getRowIndex()));
						} else {
							columnMerge.setLastRow(cell.getRowIndex());
						}
					}
				}
				if (lastCell(cell)) {
					for (Integer columnIndex : columnMergeRow.keySet()) {
						List<ColumnMerge> columnMerges = columnMergeRow.get(columnIndex);
						for (ColumnMerge columnMerge : columnMerges) {
							if (columnMerge.getFirstRow() < columnMerge.getLastRow()) {
								sheet.addMergedRegionUnsafe(new CellRangeAddress(columnMerge.getFirstRow(), columnMerge.getLastRow(), columnIndex, columnIndex));
							}
						}
					}
				}
				
			}
		}
		
		private boolean lastCell(Cell cell) {
			long totalColumnSize = graphicData.getGraphicDataColumns().size() - 1;
			long totalRowSize = graphicData.getGraphicDataColumns().stream().findFirst().get().getRowColumns().size() + graphicData.getData().size() - 1;
			return cell.getRowIndex() == totalRowSize && cell.getColumnIndex() == totalColumnSize;
		}
		
		public class ColumnMerge {
			
			private String value;
			
			private int firstRow;
			
			private int lastRow;
			
			public ColumnMerge(String value, int firstRow, int lastRow) {
				this.value = value;
				this.firstRow = firstRow;
				this.lastRow = lastRow;
			}
			
			public String getValue() {
				return value;
			}
			
			public void setValue(String value) {
				this.value = value;
			}
			
			public int getFirstRow() {
				return firstRow;
			}
			
			public int getLastRow() {
				return lastRow;
			}
			
			public void setLastRow(int lastRow) {
				this.lastRow = lastRow;
			}
			
			@Override
			public boolean equals(Object o) {
				if (o == null || getClass() != o.getClass())
					return false;
				ColumnMerge that = (ColumnMerge) o;
				return Objects.equals(value, that.value);
			}
			
			@Override
			public int hashCode() {
				return Objects.hash(value);
			}
		}
	}
}
