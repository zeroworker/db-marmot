package db.marmot.volume.controller;

import db.marmot.contorller.AbstractWebController;
import db.marmot.contorller.WebControllerAdapter;
import db.marmot.repository.DataSourceRepository;
import db.marmot.volume.DataVolume;
import db.marmot.volume.controller.request.ColumnDataRequest;
import db.marmot.volume.controller.request.DataVolumeRequest;
import db.marmot.volume.generator.ColumnData;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class VolumeControllerAdapter extends WebControllerAdapter {
	
	private DataSourceRepository dataSourceRepository;
	private ColumnGeneratorAdapter columnGeneratorAdapter;

	public VolumeControllerAdapter(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter) {
		this.dataSourceRepository = dataSourceRepository;
		this.columnGeneratorAdapter = columnGeneratorAdapter;
	}

	@Override
	public Map<String, Class> getController() {
		Map<String, Class> controllers = new HashMap<>();
		/*获取指定数据集*/
		controllers.put("/marmot/volume/getDataVolume", GetDataVolumeController.class);
		/*获取所有数据集*/
		controllers.put("/marmot/volume/getDataVolumes", GetDataVolumesController.class);
		/*获取字段数据集*/
		controllers.put("/marmot/volume/getColumnData", GetColumnDataController.class);
		return controllers;
	}
	
	/**
	 * 获取指定数据集
	 */
	public class GetDataVolumeController extends AbstractWebController<DataVolumeRequest, DataVolume> {
		
		@Override
		protected DataVolume postHandleResult(DataVolumeRequest request) {
			return dataSourceRepository.findDataVolume(request.getVolumeCode());
		}
	}
	
	/**
	 * 获取所有数据集
	 */
	public class GetDataVolumesController extends AbstractWebController<DataVolumeRequest, List<DataVolume>> {
		
		@Override
		protected List<DataVolume> postHandleResult(DataVolumeRequest request) {
			return dataSourceRepository.queryPageDataVolume(request.getVolumeName(), 0, Integer.MAX_VALUE);
		}
	}
	
	/**
	 * 获取字段数据集
	 */
	public class GetColumnDataController extends AbstractWebController<ColumnDataRequest, ColumnData> {
		
		@Override
		protected ColumnData postHandleResult(ColumnDataRequest request) {
			return columnGeneratorAdapter.generateColumnData(request.getVolumeCode(), request.getColumnCode(), request.getFilterColumns(), request.getPageNum(), request.getPageSize());
		}
	}
}
