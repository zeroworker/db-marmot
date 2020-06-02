package db.marmot.statistical.contorller;

import db.marmot.contorller.AbstractWebController;
import db.marmot.contorller.WebControllerAdapter;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalReviseTask;
import db.marmot.statistical.contorller.request.StatisticalReviseTaskRequest;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class StatisticalControllerAdapter extends WebControllerAdapter {
	
	private DataSourceRepository dataSourceRepository;
	private StatisticalGenerateAdapter statisticalGenerateAdapter;
	
	public StatisticalControllerAdapter(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		this.dataSourceRepository = dataSourceRepository;
		this.statisticalGenerateAdapter = statisticalGenerateAdapter;
	}
	
	@Override
	protected Map<String, Class> getController() {
		Map<String, Class> controllers = new HashMap<>();
		/*新增统计订正任务*/
		controllers.put("/marmot/statistical/storeStatisticalReviseTask", StoreStatisticalReviseTask.class);
		/*获取所有统计订正任务*/
		controllers.put("/marmot/statistical/getStatisticalReviseTasks", GetStatisticalReviseTasks.class);
		/*执行统计订正*/
		controllers.put("/marmot/statistical/reviseStatisticalData", ReviseStatisticalData.class);
		return controllers;
	}
	
	public class StoreStatisticalReviseTask extends AbstractWebController<StatisticalReviseTask, Void> {
		
		@Override
		protected void postHandle(StatisticalReviseTask request) {
			dataSourceRepository.storeStatisticalReviseTask(request);
		}
	}
	
	public class GetStatisticalReviseTasks extends AbstractWebController<StatisticalReviseTaskRequest, List<StatisticalReviseTask>> {
		
		@Override
		protected List<StatisticalReviseTask> postHandleResult(StatisticalReviseTaskRequest request) {
			return dataSourceRepository.queryPageStatisticalReviseTasks(request.getVolumeCode(), request.getReviseStatus(), request.getPageNum(), request.getPageSize());
		}
	}
	
	public class ReviseStatisticalData extends AbstractWebController<StatisticalReviseTaskRequest, Void> {
		@Override
		protected void postHandle(StatisticalReviseTaskRequest request) {
			statisticalGenerateAdapter.reviseStatisticalData(request.getDashboardId());
		}
	}
}
