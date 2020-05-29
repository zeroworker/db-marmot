package db.marmot.graphic.download;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.AbstractVerticalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDownload;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
@Slf4j
public abstract class GraphicDataDownloader<G extends GraphicData> implements GraphicDownloader {
	
	private ExcelWriter excelWriter;
	
	private WriteSheet writeSheet;
	
	private GraphicGeneratorAdapter graphicGeneratorAdapter;
	
	private List<WriteHandler> writeHandlers = Lists.newArrayList();
	
	public GraphicDataDownloader(GraphicGeneratorAdapter graphicGeneratorFactory) {
		this.graphicGeneratorAdapter = graphicGeneratorFactory;
		writeHandlers.add(new ColumnWidthStyleStrategy());
	}
	
	@Override
	public void downloadFile(GraphicDownload graphicDownload) {
		G graphicData = generateGraphicData(graphicDownload);
		buildExcelWriter(graphicDownload, graphicData);
		excelWriter.write(graphicData.buildFileData(), writeSheet);
		excelWriter.finish();
	}
	
	/**
	 * 支持两种图表数据生成方式,根据图表ID生成,或者根据图表设计生成
	 * @param graphicDownload
	 * @return
	 */
	private G generateGraphicData(GraphicDownload graphicDownload) {
		if (StringUtils.isNotBlank(graphicDownload.getGraphicCode())) {
			return graphicGeneratorAdapter.generateGraphicData(graphicDownload.getGraphicCode(), Boolean.FALSE.booleanValue());
		}
		Graphic graphic = graphicDownload.getGraphic();
		graphic.setGraphicFormat(Boolean.FALSE.booleanValue());
		return graphicGeneratorAdapter.generateGraphicData(graphicDownload.getVolumeCode(), graphicDownload.getGraphicType(), graphic);
	}
	
	/**
	 * 构建 excel writer
	 * @param graphicDownload
	 * @param graphicData
	 */
	private void buildExcelWriter(GraphicDownload graphicDownload, G graphicData) {
		if (excelWriter == null) {
			if (writeHandlers.isEmpty()) {
				//-根据生成的图表数据创建样式,只创建一次
				writeHandlers.add(new ExcelVerticalCellStyleStrategy(graphicData));
				registerWriteHandler(graphicData, writeHandlers);
			}
			
			//-若存在重复文件,直接覆盖
			ExcelWriterBuilder writerBuilder = EasyExcel.write(graphicDownload.getFileUrl());
			writerBuilder.head(graphicData.buildFileHead());
			writeHandlers.forEach(writeHandler -> writerBuilder.registerWriteHandler(writeHandler));
			this.excelWriter = writerBuilder.build();
			this.writeSheet = EasyExcel.writerSheet(graphicDownload.getFileName()).build();
		}
	}
	
	/**
	 * 注册样式处理器
	 * @param graphicData
	 */
	public abstract void registerWriteHandler(G graphicData, List<WriteHandler> writeHandlers);
	
	class ExcelVerticalCellStyleStrategy extends AbstractVerticalCellStyleStrategy {
		
		private GraphicData graphicData;
		
		public ExcelVerticalCellStyleStrategy(GraphicData graphicData) {
			this.graphicData = graphicData;
		}
		
		@Override
		protected WriteCellStyle headCellStyle(Head head) {
			WriteCellStyle writeCellStyle = new WriteCellStyle();
			writeCellStyle.setBorderTop(BorderStyle.THIN);
			writeCellStyle.setBorderLeft(BorderStyle.THIN);
			writeCellStyle.setBorderBottom(BorderStyle.THIN);
			writeCellStyle.setBorderRight(BorderStyle.THIN);
			writeCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			writeCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
			WriteFont writeFont = new WriteFont();
			writeFont.setFontHeightInPoints((short) 14);
			writeFont.setFontName("宋体");
			writeFont.setBold(false);
			writeFont.setColor(graphicData.getGraphicDataColumns().get(head.getColumnIndex()).getDataColor().getColor());
			writeCellStyle.setWriteFont(writeFont);
			return writeCellStyle;
		}
		
		@Override
		protected WriteCellStyle contentCellStyle(Head head) {
			WriteCellStyle writeCellStyle = new WriteCellStyle();
			writeCellStyle.setBorderLeft(BorderStyle.THIN);
			writeCellStyle.setBorderTop(BorderStyle.THIN);
			writeCellStyle.setBorderRight(BorderStyle.THIN);
			writeCellStyle.setBorderBottom(BorderStyle.THIN);
			writeCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
			writeCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			WriteFont writeFont = new WriteFont();
			writeFont.setFontName("宋体");
			writeFont.setBold(false);
			writeFont.setFontHeightInPoints((short) 14);
			writeFont.setColor(graphicData.getGraphicDataColumns().get(head.getColumnIndex()).getDataColor().getColor());
			writeCellStyle.setWriteFont(writeFont);
			return writeCellStyle;
		}
	}
	
	class ColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {
		
		private final int MAX_COLUMN_WIDTH = 255;
		
		private final Map<Integer, Map<Integer, Integer>> CACHE = Maps.newHashMap();
		
		@Override
		protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
			Map<Integer, Integer> maxColumnWidthMap = CACHE.get(writeSheetHolder.getSheetNo());
			if (maxColumnWidthMap == null) {
				maxColumnWidthMap = Maps.newHashMap();
				CACHE.put(writeSheetHolder.getSheetNo(), maxColumnWidthMap);
			}
			
			Integer columnWidth = cell.getStringCellValue().getBytes().length + 1;
			columnWidth = columnWidth > MAX_COLUMN_WIDTH ? MAX_COLUMN_WIDTH : columnWidth;
			Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
			
			if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
				maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
				writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
			}
		}
	}
}
