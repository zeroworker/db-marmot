package db.marmot.graphic.contorller;

import db.marmot.contorller.AbstractWebController;
import db.marmot.contorller.WebControllerAdapter;
import db.marmot.graphic.Dashboard;
import db.marmot.graphic.contorller.request.DashboardRequest;
import db.marmot.repository.DataSourceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class DashboardControllerAdapter extends WebControllerAdapter {
	
	private DataSourceRepository dataSourceRepository;

	public DashboardControllerAdapter(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	public Map<String, Class> getController() {
		Map<String, Class> controllers = new HashMap<>();
		/*保存仪表盘*/
		controllers.put("/marmot/dashBord/storeDashboards", StoreDashBordController.class);
		/*获取所有仪表盘*/
		controllers.put("/marmot/dashBord/getDashboards", GetDashboardsController.class);
		/*获取指定仪表盘*/
		controllers.put("/marmot/dashBord/getDashboard", GetDashboardController.class);
		/*删除指定仪表盘*/
		controllers.put("/marmot/dashBord/deleteDashboard", DeleteDashboardController.class);
		return controllers;
	}
	
	/**
	 * 保存仪表盘
	 */
	public class StoreDashBordController extends AbstractWebController<Dashboard, Void> {
		
		@Override
		protected void postHandle(Dashboard request) {
			dataSourceRepository.storeDashboard(request);
		}
	}
	
	/**
	 * 获取所有仪表盘
	 */
	public class GetDashboardsController extends AbstractWebController<DashboardRequest, List<Dashboard>> {
		
		@Override
		protected List<Dashboard> postHandleResult(DashboardRequest request) {
			return dataSourceRepository.queryPageDashboard(request.getFounderId(), request.getBoardName(), request.getBoardType(), request.getPageNum(), request.getPageSize());
		}
	}
	
	/**
	 * 获取指定仪表盘
	 */
	public class GetDashboardController extends AbstractWebController<DashboardRequest, Dashboard> {
		
		@Override
		protected Dashboard postHandleResult(DashboardRequest request) {
			return dataSourceRepository.findDashboard(request.getDashboardId());
		}
	}
	
	/**
	 * 获取指定仪表盘
	 */
	public class DeleteDashboardController extends AbstractWebController<DashboardRequest, Dashboard> {
		
		@Override
		protected void postHandle(DashboardRequest request) {
			dataSourceRepository.deleteDashboard(request.getDashboardId());
		}
	}
}
