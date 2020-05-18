package db.marmot.graphic.download;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDownload;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
}
