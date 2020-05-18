package db.marmot.graphic.contorller;

import com.alibaba.fastjson.JSONObject;
import db.marmot.contorller.AbstractWebController;
import db.marmot.contorller.WebControllerAdapter;
import db.marmot.enums.GraphicType;
import db.marmot.graphic.GraphicDownload;
import db.marmot.graphic.contorller.request.GraphicDataRequest;
import db.marmot.graphic.contorller.request.GraphicRequest;
import db.marmot.graphic.contorller.request.TabGraphicDataRequest;
import db.marmot.graphic.download.GraphicDownloadAdapter;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shaokang
 */
public class GraphicDataControllerAdapter extends WebControllerAdapter {
	
	private GraphicDownloadAdapter graphicDownloadAdapter;
	private GraphicGeneratorAdapter graphicGeneratorAdapter;
	private Map<String, Class> graphicTypeRequest = new HashMap<>();
	
	public void setGraphicDownloadAdapter(GraphicDownloadAdapter graphicDownloadAdapter) {
		this.graphicDownloadAdapter = graphicDownloadAdapter;
	}
	
	public void setGraphicGeneratorAdapter(GraphicGeneratorAdapter graphicGeneratorAdapter) {
		this.graphicGeneratorAdapter = graphicGeneratorAdapter;
	}
	
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		//-注册图表request类型
		graphicTypeRequest.put(GraphicType.cross_tab.getCode(), TabGraphicDataRequest.class);
	}
	
	@Override
	public Map<String, Class> getController() {
		Map<String, Class> controllers = new HashMap<>();
		/*获取指定图表数据*/
		controllers.put("/marmot/graphic/getGraphicDataById", GetGraphicDataByIdController.class);
		/*下载指定图表数据*/
		controllers.put("/marmot/graphic/downloadGraphicDataById", DownloadGraphicDataByIdController.class);
		/*获取图表数据*/
		controllers.put("/marmot/graphic/getGraphicData", GetGraphicDataController.class);
		/*下载图表数据*/
		controllers.put("/marmot/graphic/downloadGraphicData", DownloadGraphicDataController.class);
		return controllers;
	}
	
	/**
	 * 根据请求json 获取 request class 类型
	 * @param requestJson
	 * @return
	 */
	private Class getGraphicRequestClass(String requestJson) {
		//-图表请求 根据图表类型获取指定的图表类class做为反序列化class 若不存在,使用父类
		String graphicType = JSONObject.parseObject(requestJson).getString("graphicType");
		Class graphicRequestClass = graphicTypeRequest.get(graphicType);
		return graphicRequestClass != null ? graphicRequestClass : GraphicDataRequest.class;
	}
	
	/**
	 * 获取指定图表数据
	 */
	public class GetGraphicDataByIdController extends AbstractWebController<GraphicRequest, GraphicData> {
		
		@Override
		protected GraphicData postHandleResult(GraphicRequest request) {
			return graphicGeneratorAdapter.generateGraphicData(request.getGraphicCode(), true);
		}
	}
	
	/**
	 * 下载指定图表数据
	 */
	public class DownloadGraphicDataByIdController extends AbstractWebController<GraphicRequest, GraphicDownload> {
		
		@Override
		protected GraphicDownload postHandleResult(GraphicRequest request) {
			if (StringUtils.isNotBlank(request.getFounderId())) {
				return graphicDownloadAdapter.downloadGraphicData(request.getFounderId(), request.getGraphicCode());
			}
			return graphicDownloadAdapter.downloadGraphicData(request.getGraphicCode());
		}
	}
	
	/**
	 * 获取图表数据
	 */
	public class GetGraphicDataController extends AbstractWebController<GraphicDataRequest, GraphicData> {
		
		@Override
		protected Class<GraphicDataRequest> deserializeClass(String requestJson) {
			return getGraphicRequestClass(requestJson);
		}
		
		@Override
		protected GraphicData postHandleResult(GraphicDataRequest request) {
			request.getGraphic().setGraphicFormat(true);
			return graphicGeneratorAdapter.generateGraphicData(request.getVolumeCode(), request.getGraphicType(), request.getGraphic());
		}
	}
	
	/**
	 * 下载图表数据
	 */
	public class DownloadGraphicDataController extends AbstractWebController<GraphicDataRequest, GraphicDownload> {
		
		@Override
		protected Class<GraphicDataRequest> deserializeClass(String requestJson) {
			return getGraphicRequestClass(requestJson);
		}
		
		@Override
		protected GraphicDownload postHandleResult(GraphicDataRequest request) {
			if (StringUtils.isNotBlank(request.getFounderId())) {
				return graphicDownloadAdapter.downloadGraphicData(request.getVolumeCode(), request.getGraphicName(), request.getGraphicType(), request.getGraphic());
			}
			return graphicDownloadAdapter.downloadGraphicData(request.getFounderId(), request.getVolumeCode(), request.getGraphicName(), request.getGraphicType(), request.getGraphic());
		}
	}
}
