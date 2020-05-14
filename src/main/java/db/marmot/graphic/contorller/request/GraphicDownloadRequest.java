package db.marmot.graphic.contorller.request;

import db.marmot.enums.DownloadStatus;
import db.marmot.enums.GraphicType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GraphicDownloadRequest {
	
	/**
	 * 下载ID
	 */
	private long downloadId;
	
	/**
	 * 创建人ID
	 */
	private String founderId;
	
	/**
	 * 文件名
	 */
	private String fileName;
	
	/**
	 * 图表类型
	 */
	private GraphicType graphicType;
	
	/**
	 * 下载状态
	 */
	private DownloadStatus status;
	
	/**
	 * 分页数
	 */
	private int pageNum;
	
	/**
	 * 分页大小
	 */
	private int pageSize;
}
