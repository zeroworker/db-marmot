package db.marmot.graphic.contorller;

import db.marmot.contorller.AbstractWebController;
import db.marmot.contorller.WebControllerAdapter;
import db.marmot.graphic.GraphicDownload;
import db.marmot.graphic.contorller.request.GraphicDownloadRequest;
import db.marmot.repository.DataSourceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class GraphicDownloadControllerAdapter extends WebControllerAdapter {
	
	private DataSourceRepository dataSourceRepository;

	public GraphicDownloadControllerAdapter(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	public Map<String, Class> getController() {
		Map<String, Class> controllers = new HashMap<>();
		/*获取所有图表下载任务*/
		controllers.put("/marmot/graphicDownload/getGraphicDownloads", GetGraphicDownloadsController.class);
		/*获取指定图表下载任务*/
		controllers.put("/marmot/graphicDownload/getGraphicDownload", GetGraphicDownloadController.class);
		/*删除指定图表下载任务*/
		controllers.put("/marmot/graphicDownload/deleteGraphicDownload", DeleteGraphicDownloadController.class);
		
		return controllers;
	}
	
	/**
	 * 获取所有图表下载任务
	 */
	public class GetGraphicDownloadsController extends AbstractWebController<GraphicDownloadRequest, List<GraphicDownload>> {
		
		@Override
		protected List<GraphicDownload> postHandleResult(GraphicDownloadRequest request) {
			return dataSourceRepository.queryPageGraphicDownloads(request.getFounderId(), request.getFileName(), request.getGraphicType(), request.getStatus(), request.getPageNum(), request.getPageSize());
		}
	}
	
	/**
	 * 获取指定图表下载任务
	 */
	public class GetGraphicDownloadController extends AbstractWebController<GraphicDownloadRequest, GraphicDownload> {
		
		@Override
		protected GraphicDownload postHandleResult(GraphicDownloadRequest request) {
			return dataSourceRepository.findGraphicDownload(request.getDownloadId());
		}
	}
	
	/**
	 * 删除指定图表下载任务
	 */
	public class DeleteGraphicDownloadController extends AbstractWebController<GraphicDownloadRequest, Void> {
		
		@Override
		protected void postHandle(GraphicDownloadRequest request) {
			dataSourceRepository.deleteGraphicDownload(request.getDownloadId());
		}
	}
	
}
