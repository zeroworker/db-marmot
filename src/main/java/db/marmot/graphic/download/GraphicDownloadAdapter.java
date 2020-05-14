package db.marmot.graphic.download;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDownload;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import db.marmot.repository.RepositoryAdapter;

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
	 * 设置下载监听
	 * @param graphicDownloadListener
	 */
	void setGraphicDownloadListener(GraphicDownloadListener graphicDownloadListener);
	
	/**
	 * 设置仓库
	 * @param repositoryAdapter
	 */
	void setRepositoryAdapter(RepositoryAdapter repositoryAdapter);
	
	/**
	 * 设置图表适配器
	 * @param graphicGeneratorAdapter
	 */
	void setGraphicGeneratorAdapter(GraphicGeneratorAdapter graphicGeneratorAdapter);
	
	/**
	 * 下载等待中的图表数据
	 */
	void downloadWaitGraphicData();
	
	/**
	 * 未指定图表创建人,直接下载图表数据
	 * @return
	 */
	GraphicDownload downloadGraphicData(long graphicId);
	
	/**
	 * 指定图表创建人,异步下载
	 * @return
	 */
	GraphicDownload downloadGraphicData(String founderId, long graphicId);
	
	/**
	 * 未指定图表创建人,直接下载图表数据
	 * @param volumeId
	 * @param graphicType
	 * @param graphicName
	 * @param graphic
	 * @return
	 */
	GraphicDownload downloadGraphicData(long volumeId, String graphicName, GraphicType graphicType, Graphic graphic);
	
	/**
	 * 指定图表创建人,异步下载
	 * @param founderId
	 * @param volumeId
	 * @param graphicType
	 * @param graphic
	 * @return
	 */
	GraphicDownload downloadGraphicData(String founderId, long volumeId, String graphicName, GraphicType graphicType, Graphic graphic);
	
}
