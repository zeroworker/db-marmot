package db.marmot.graphic.download;

import db.marmot.graphic.GraphicDownload;

/**
 * @author shaokang
 */
public interface GraphicDownloadListener {
	
	/**
	 * 图表下载开始
	 * @param graphicDownload
	 */
	void downloadStart(GraphicDownload graphicDownload);
	
	/**
	 * 图表下载完成 下载成功/失败 触发监听
	 * @param graphicDownload
	 */
	void downloadEnd(GraphicDownload graphicDownload);
}
