package db.marmot.graphic;

import db.marmot.enums.*;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModelBuilder;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shaokang
 */
public class GraphicRepository {
	
	protected DataSourceTemplate dataSourceTemplate;
	private static final AtomicLong seqGen = new AtomicLong(System.currentTimeMillis());
	
	public GraphicRepository(DataSourceTemplate dataSourceTemplate) {
		this.dataSourceTemplate = dataSourceTemplate;
	}
	
	/**
	 * 保存仪表盘
	 * @param dashboard 仪表盘
	 */
	public void storeDashboard(Dashboard dashboard) {
		Validators.notNull(dashboard, "仪表盘不能为空");
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(dashboard.getVolumeCode());
		Validators.notNull(dataVolume, "数据集%s不存在", dataVolume.getVolumeCode());
		Database database = dataSourceTemplate.findDatabase(dataVolume.getDbName());
		Validators.notNull(database, "数据集数据源%s不存在", dataVolume.getDbName());
		dashboard.validateDashboard(dataVolume);
		try {
			dataSourceTemplate.storeDashboard(dashboard);
		} catch (DuplicateKeyException keyException) {
			Dashboard originalDashboard = dataSourceTemplate.findDashboard(dashboard.getBoardId());
			Validators.notNull(originalDashboard, "仪表盘%s不存在", dashboard.getBoardName());
			if (originalDashboard.getVolumeCode() != dashboard.getVolumeCode()) {
				DataVolume originalDataVolume = dataSourceTemplate.findDataVolume(dashboard.getVolumeCode());
				Validators.notNull(originalDataVolume, "数据集%s不存在", dashboard.getVolumeCode());
				if (originalDataVolume.getVolumeType() == VolumeType.model) {
					List<GraphicDesign> graphicDesigns = dataSourceTemplate.queryGraphicDesign(dashboard.getBoardId());
					if (graphicDesigns != null && graphicDesigns.size() > 0) {
						graphicDesigns.forEach(graphicDesign -> {
							dataSourceTemplate.deleteStatisticalModel(graphicDesign.getGraphic().getModelName());
						});
					}
				}
				dataSourceTemplate.deleteGraphicDesignByBoardId(dashboard.getBoardId());
			}
			dataSourceTemplate.updateDashboard(dashboard);
		}
		if (dashboard.getGraphicDesigns() != null && !dashboard.getGraphicDesigns().isEmpty()) {
			for (GraphicDesign graphicDesign : dashboard.getGraphicDesigns()) {
				try {
					graphicDesign.setBoardId(dashboard.getBoardId());
					graphicDesign.setGraphicCode(StringUtils.join(dashboard.getBoardId() + "_" + seqGen.incrementAndGet()));
					dataSourceTemplate.storeGraphicDesign(graphicDesign);
					if (dataVolume.getVolumeType() == VolumeType.model) {
						StatisticalModelBuilder builder = new StatisticalModelBuilder().addMemo(graphicDesign.getGraphicName()).addWindowUnit(WindowUnit.day).addWindowLength(0)
							.addWindowType(WindowType.simple_time).addModelName(graphicDesign.getGraphicCode()).addDataVolume(dataVolume);
						dataSourceTemplate.storeStatisticalModel(graphicDesign.getGraphic().configurationModel(builder));
					}
				} catch (DuplicateKeyException keyException) {
					Validators.isTrue(dataVolume.getVolumeType() != VolumeType.model, "模型统计数据源图表不支持更新");
					GraphicDesign originalGraphicDesign = dataSourceTemplate.findGraphicDesign(graphicDesign.getGraphicId());
					Validators.notNull(originalGraphicDesign,"重复图表 %s",graphicDesign.getGraphicName());
					if (dataVolume.getVolumeType() == VolumeType.sql) {
						dataSourceTemplate.updateGraphicDesign(graphicDesign);
					}
				}
			}
		}
		List<GraphicDesign> graphicDesigns = dataSourceTemplate.queryGraphicDesign(dashboard.getBoardId());
		for (GraphicDesign graphicDesign : graphicDesigns) {
			boolean deleteGraphicDesign = Boolean.TRUE.booleanValue();
			for (GraphicDesign design : dashboard.getGraphicDesigns()) {
				if (design.getGraphicId() == graphicDesign.getGraphicId()) {
					deleteGraphicDesign = Boolean.FALSE.booleanValue();
					break;
				}
			}
			if (deleteGraphicDesign) {
				dataSourceTemplate.deleteGraphicDesignByGraphicId(graphicDesign.getGraphicId());
				if (dataVolume.getVolumeType() == VolumeType.model) {
					dataSourceTemplate.deleteStatisticalModel(graphicDesign.getGraphic().getModelName());
				}
			}
		}
	}
	
	/**
	 * 根据仪表盘ID删除仪表盘
	 * @param boardId 仪表盘ID
	 */
	public void deleteDashboard(long boardId) {
		Dashboard dashboard = dataSourceTemplate.findDashboard(boardId);
		Validators.notNull(dashboard, "仪表盘不存在");
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(dashboard.getVolumeCode());
		Validators.notNull(dataVolume, "数据集%s不存在", dashboard.getVolumeCode());
		if (dataVolume.getVolumeType() == VolumeType.model) {
			List<GraphicDesign> graphicDesigns = dataSourceTemplate.queryGraphicDesign(dashboard.getBoardId());
			if (graphicDesigns != null && graphicDesigns.size() > 0) {
				for (GraphicDesign graphicDesign : graphicDesigns) {
					dataSourceTemplate.deleteStatisticalModel(graphicDesign.getGraphic().getModelName());
				}
			}
		}
		dataSourceTemplate.deleteDashboard(boardId);
		dataSourceTemplate.deleteGraphicDesignByBoardId(boardId);
	}
	
	/**
	 * 查询仪表盘
	 * @param founderId 创建人ID
	 * @param boardName 仪表盘名称
	 * @param boardType 仪表盘类型
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return
	 */
	public List<Dashboard> queryPageDashboard(String founderId, String boardName, BoardType boardType, int pageNum, int pageSize) {
		return dataSourceTemplate.queryPageDashboard(founderId, boardName, boardType != null ? boardType.getCode() : null, pageNum, pageSize);
	}
	
	/**
	 * 根据仪表盘ID获取仪表盘信息
	 * @param boardId 仪表盘ID
	 */
	public Dashboard findDashboard(long boardId) {
		Dashboard dashboard = dataSourceTemplate.findDashboard(boardId);
		Validators.notNull(dashboard, "仪表盘不存在");
		dashboard.setGraphicDesigns(dataSourceTemplate.queryGraphicDesign(boardId));
		return dashboard;
	}
	
	/**
	 * 查询图表设计
	 * @param graphicCode 图表编码
	 * @return
	 */
	public GraphicDesign findGraphicDesign(String graphicCode) {
		GraphicDesign graphicDesign = dataSourceTemplate.findGraphicDesign(graphicCode);
		Validators.notNull(graphicDesign, "图表%s不存在", graphicCode);
		return graphicDesign;
	}
	
	/**
	 * 保持图表导出任务
	 * @param graphicDownload
	 */
	public void storeGraphicDownload(GraphicDownload graphicDownload) {
		Validators.notNull(graphicDownload, "图表下载任务不能为空");
		graphicDownload.validateGraphicDownload();
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(graphicDownload.getVolumeCode());
		Validators.notNull(dataVolume, "数据集%s不存在", dataVolume.getVolumeCode());
		dataSourceTemplate.storeGraphicDownload(graphicDownload);
	}
	
	/**
	 * 更新图表下载任务为下载中状态
	 * @param graphicDownload
	 */
	public void updateGraphicDownloadIng(GraphicDownload graphicDownload) {
		GraphicDownload originalGraphicDownload = dataSourceTemplate.loadGraphicDownload(graphicDownload.getDownloadId());
		Validators.notNull(graphicDownload, "图表下载任务不存在");
		Validators.isTrue(originalGraphicDownload.getStatus() == DownloadStatus.download_wait,"图表下载任务非下载等待状态[%s]",originalGraphicDownload.getStatus().getMessage());
		dataSourceTemplate.updateGraphicDownload(graphicDownload.downloadIng());
	}
	
	/**
	 * 更新图表下载任务
	 * @param graphicDownload
	 */
	public void updateGraphicDownload(GraphicDownload graphicDownload) {
		GraphicDownload originalGraphicDownload = dataSourceTemplate.loadGraphicDownload(graphicDownload.getDownloadId());
		Validators.notNull(originalGraphicDownload, "图表下载任务不存在");
		graphicDownload.validateGraphicDownload();
		dataSourceTemplate.updateGraphicDownload(graphicDownload);
	}
	
	/**
	 * 删除图表下载任务
	 * @param downloadId
	 */
	public void deleteGraphicDownload(long downloadId) {
		GraphicDownload graphicDownload = dataSourceTemplate.findGraphicDownload(downloadId);
		Validators.notNull(graphicDownload, "图表下载任务不存在");
		File file = new File(graphicDownload.getFileUrl());
		if (file.exists()) {
			Validators.isTrue(file.isFile(), "下载文件路径非文件");
			Validators.isTrue(file.delete(), "删除下载文件失败");
		}
		dataSourceTemplate.deleteGraphicDownload(downloadId);
	}
	
	/**
	 * 图表下载任务不存在
	 * @param downloadId
	 * @return
	 */
	public GraphicDownload findGraphicDownload(long downloadId) {
		GraphicDownload graphicDownload = dataSourceTemplate.findGraphicDownload(downloadId);
		Validators.notNull(graphicDownload, "图表下载任务不存在");
		return graphicDownload;
	}
	
	/**
	 * 查询等待图表下载任务
	 * @param pageSize
	 * @return
	 */
	public List<GraphicDownload> queryPageGraphicDownloads(String founderId, String fileName, GraphicType graphicType, DownloadStatus status, int pageNum, int pageSize) {
		return dataSourceTemplate.queryPageGraphicDownloads(founderId, fileName, graphicType, status, OrderType.desc, pageNum, pageSize);
	}
	
	/**
	 * 查询图表导出列表
	 * @param pageSize
	 * @return
	 */
	public List<GraphicDownload> queryWaitGraphicDownloads(int pageSize) {
		return dataSourceTemplate.queryPageGraphicDownloads(null, null, null, DownloadStatus.download_wait, OrderType.asc, 0, pageSize);
	}
}
