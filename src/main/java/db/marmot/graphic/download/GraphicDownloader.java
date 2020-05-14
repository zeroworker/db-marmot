package db.marmot.graphic.download;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.GraphicDownload;

/**
 * @author shaokang
 */
public interface GraphicDownloader {
	
	/**
	 * 图表类型
	 * @return
	 */
	GraphicType graphicType();
	
	/**
	 * 下载文件
	 * @param graphicDownload
	 */
	void downloadFile(GraphicDownload graphicDownload);
}
