package db.marmot.graphic.download;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDownload;

/**
 * @author shaokang
 */
public interface GraphicDownloadAdapter {
	
	/**
	 * 设置文件存储路径
	 * @param fileUrl
	 */
	void setFileUrl(String fileUrl);
	
	/**
	 * 设置并发下载数量
	 * @param downloadNum
	 */
	void setDownloadNum(int downloadNum);
	
	/**
	 * 设置下载地址
	 * @param downloadUrl
	 */
	void setDownloadUrl(String downloadUrl);
	
	/**
	 * 下载等待中的图表数据
	 */
	void downloadWaitGraphicData();
	
	/**
	 * 未指定图表创建人,直接下载图表数据
	 * @param graphicCode
	 * @return
	 */
	GraphicDownload downloadGraphicData(String graphicCode);
	
	/**
	 * 指定图表创建人,异步下载
	 * @param graphicCode
	 * @return
	 */
	GraphicDownload downloadGraphicData(String founderId, String graphicCode);
	
	/**
	 * 未指定图表创建人,直接下载图表数据
	 * @param volumeCode
	 * @param graphicType
	 * @param graphicName
	 * @param graphic
	 * @return
	 */
	GraphicDownload downloadGraphicData(String volumeCode, String graphicName, GraphicType graphicType, Graphic graphic);
	
	/**
	 * 指定图表创建人,异步下载
	 * @param founderId
	 * @param volumeCode
	 * @param graphicType
	 * @param graphic
	 * @return
	 */
	GraphicDownload downloadGraphicData(String founderId, String volumeCode, String graphicName, GraphicType graphicType, Graphic graphic);
	
}
