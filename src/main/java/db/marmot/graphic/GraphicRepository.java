package db.marmot.graphic;

import db.marmot.enums.*;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTemplate;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;
import db.marmot.volume.DatabaseTemplate;
import db.marmot.volume.VolumeTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class GraphicRepository extends DataSourceRepository {
	
	private VolumeTemplate volumeTemplate;
	private GraphicTemplate graphicTemplate;
	private StatisticalTemplate statisticalTemplate;
	private DatabaseTemplate databaseTemplate;
	
	public GraphicRepository(Map<TemplateType, DataSourceTemplate> templates) {
		super(templates);
		this.volumeTemplate = getTemplate(TemplateType.volume);
		this.graphicTemplate = getTemplate(TemplateType.graphic);
		this.statisticalTemplate = getTemplate(TemplateType.statistical);
		this.databaseTemplate = getTemplate(TemplateType.database);
	}
	
	/**
	 * 保存仪表盘
	 * @param dashboard 仪表盘
	 */
	public void storeDashboard(Dashboard dashboard) {
		//-保存仪表盘-仪表盘包含多图表设计,顾将新增和更新仪表盘统一处理,提交时提交多图表设计以及仪表盘信息
		if (dashboard == null) {
			throw new RepositoryException("仪表盘不能为空");
		}
		DataVolume dataVolume = volumeTemplate.findDataVolume(dashboard.getVolumeCode());
		if (dataVolume == null) {
			throw new RepositoryException("数据集不存在");
		}
		
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException("数据集数据源不存在");
		}
		dashboard.validateDashboard(dataVolume);
		try {
			graphicTemplate.storeDashboard(dashboard);
		} catch (DuplicateKeyException keyException) {
			Dashboard originalDashboard = graphicTemplate.findDashboard(dashboard.getBoardId());
			//-1.出现幂等异常 根据仪表盘ID获取仪表盘,存在视为更新,不存在视为重复保存
			if (originalDashboard == null) {
				throw new RepositoryException(String.format("重复仪表盘 %s", dashboard.getBoardName()));
			}
			//- 2.数据源存在变更处理
			if (originalDashboard.getVolumeCode() != dashboard.getVolumeCode()) {
				//-2.1 数据源存在变更 原始图表设计已经无用,删除
				DataVolume originalDataVolume = volumeTemplate.findDataVolume(dashboard.getVolumeCode());
				if (originalDataVolume == null) {
					throw new RepositoryException(String.format("数据集不存在"));
				}
				if (originalDataVolume.getVolumeType() == VolumeType.model) {
					//-正在运行的统计模型已经无用,可以删除了,删除成本高,同步禁用 异步删除
					List<GraphicDesign> graphicDesigns = graphicTemplate.queryGraphicDesign(dashboard.getBoardId());
					if (graphicDesigns != null && graphicDesigns.size() > 0) {
						graphicDesigns.forEach(graphicDesign -> {
							List<String> modelNames = graphicDesign.getGraphic().getModelNames();
							modelNames.forEach(modelName -> statisticalTemplate.deleteStatisticalModel(modelName));
						});
					}
				}
				graphicTemplate.deleteGraphicDesignByBoardId(dashboard.getBoardId());
			}
			//-3.仪表盘更新修改信息
			graphicTemplate.updateDashboard(dashboard);
		}
		//-保存图表-因为支持新增以及更新以及需要支持模型统计,顾不能全部删除图表在信息
		if (dashboard.getGraphicDesigns() != null && !dashboard.getGraphicDesigns().isEmpty()) {
			for (GraphicDesign graphicDesign : dashboard.getGraphicDesigns()) {
				try {
					graphicDesign.createGraphicCode().setBoardId(dashboard.getBoardId());
					graphicTemplate.storeGraphicDesign(graphicDesign);
					if (dataVolume.getVolumeType() == VolumeType.model) {
						String graphicName = StringUtils.join(dashboard.getBoardId(), "-", graphicDesign.getGraphicName());
						List<StatisticalModel> statisticalModels = graphicDesign.getGraphic().createStatisticalModels(dataVolume, database.getDbType(), graphicName);
						if (statisticalModels != null && statisticalModels.size() > 0) {
							statisticalModels.forEach(statisticalModel -> statisticalTemplate.storeStatisticalModel(statisticalModel));
						}
					}
				} catch (DuplicateKeyException keyException) {
					GraphicDesign originalGraphicDesign = graphicTemplate.findGraphicDesign(graphicDesign.getGraphicId());
					if (originalGraphicDesign == null) {
						throw new RepositoryException(String.format("重复图表 %s", graphicDesign.getGraphicName()));
					}
					if (dataVolume.getVolumeType() == VolumeType.sql) {
						graphicTemplate.updateGraphicDesign(graphicDesign);
					}
					if (dataVolume.getVolumeType() == VolumeType.model) {
						throw new RepositoryException("模型统计数据源图表不支持更新");
					}
				}
			}
		}
		//- 因为当图表为模型统计时,不能直接删除,只能做更新处理,更新后,图表数量和保存时不一致,可能减少,将多余的图表删除
		List<GraphicDesign> graphicDesigns = graphicTemplate.queryGraphicDesign(dashboard.getBoardId());
		for (GraphicDesign graphicDesign : graphicDesigns) {
			boolean deleteGraphicDesign = Boolean.TRUE.booleanValue();
			for (GraphicDesign design : dashboard.getGraphicDesigns()) {
				if (design.getGraphicId() == graphicDesign.getGraphicId()) {
					deleteGraphicDesign = Boolean.FALSE.booleanValue();
					break;
				}
			}
			if (deleteGraphicDesign) {
				graphicTemplate.deleteGraphicDesignByGraphicId(graphicDesign.getGraphicId());
				if (dataVolume.getVolumeType() == VolumeType.model) {
					//-正在运行的统计模型已经无用,可以删除了,删除成本高,同步禁用 异步删除
					List<String> modelNames = graphicDesign.getGraphic().getModelNames();
					modelNames.forEach(modelName -> statisticalTemplate.deleteStatisticalModel(modelName));
				}
			}
		}
	}
	
	/**
	 * 根据仪表盘ID删除仪表盘
	 * @param boardId 仪表盘ID
	 */
	public void deleteDashboard(long boardId) {
		Dashboard dashboard = graphicTemplate.findDashboard(boardId);
		if (graphicTemplate.findDashboard(boardId) == null) {
			throw new RepositoryException(String.format("仪表盘不存在"));
		}
		DataVolume dataVolume = volumeTemplate.findDataVolume(dashboard.getVolumeCode());
		if (dataVolume == null) {
			throw new RepositoryException(String.format("数据集不存在"));
		}
		if (dataVolume.getVolumeType() == VolumeType.model) {
			//-正在运行的统计模型已经无用,可以删除了,删除成本高,同步禁用 异步删除
			List<GraphicDesign> graphicDesigns = graphicTemplate.queryGraphicDesign(dashboard.getBoardId());
			if (graphicDesigns != null && graphicDesigns.size() > 0) {
				for (GraphicDesign graphicDesign : graphicDesigns) {
					List<String> modelNames = graphicDesign.getGraphic().getModelNames();
					modelNames.forEach(modelName -> statisticalTemplate.deleteStatisticalModel(modelName));
				}
			}
		}
		graphicTemplate.deleteDashboard(boardId);
		graphicTemplate.deleteGraphicDesignByBoardId(boardId);
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
		return graphicTemplate.queryPageDashboard(founderId, boardName, boardType != null ? boardType.getCode() : null, pageNum, pageSize);
	}
	
	/**
	 * 根据仪表盘ID获取仪表盘信息
	 * @param boardId 仪表盘ID
	 */
	public Dashboard findDashboard(long boardId) {
		Dashboard dashboard = graphicTemplate.findDashboard(boardId);
		if (dashboard == null) {
			throw new RepositoryException(String.format("仪表盘不存在"));
		}
		dashboard.setGraphicDesigns(graphicTemplate.queryGraphicDesign(boardId));
		return dashboard;
	}
	
	/**
	 * 查询图表设计
	 * @param graphicCode 图表编码
	 * @return
	 */
	public GraphicDesign findGraphicDesign(String graphicCode) {
		GraphicDesign graphicDesign = graphicTemplate.findGraphicDesign(graphicCode);
		if (graphicDesign == null) {
			throw new RepositoryException(String.format("图表%s不存在", graphicCode));
		}
		return graphicDesign;
	}
	
	/**
	 * 保持图表导出任务
	 * @param graphicDownload
	 */
	public void storeGraphicDownload(GraphicDownload graphicDownload) {
		if (graphicDownload == null) {
			throw new RepositoryException(String.format("图表下载任务不能为空"));
		}
		graphicDownload.validateGraphicDownload();
		DataVolume dataVolume = volumeTemplate.findDataVolume(graphicDownload.getVolumeCode());
		if (dataVolume == null) {
			throw new RepositoryException("数据集不存在");
		}
		graphicTemplate.storeGraphicDownload(graphicDownload);
	}
	
	/**
	 * 更新图表下载任务为下载中状态
	 * @param graphicDownload
	 */
	public void updateGraphicDownloadIng(GraphicDownload graphicDownload) {
		GraphicDownload originalGraphicDownload = graphicTemplate.loadGraphicDownload(graphicDownload.getDownloadId());
		if (originalGraphicDownload == null) {
			throw new RepositoryException(String.format("图表下载任务不能为空"));
		}
		if (originalGraphicDownload.getStatus() != DownloadStatus.download_wait) {
			throw new RepositoryException(String.format("图表下载任务非下载等待状态[%s]", originalGraphicDownload.getStatus().getMessage()));
		}
		graphicTemplate.updateGraphicDownload(graphicDownload.downloadIng());
	}
	
	/**
	 * 更新图表下载任务
	 * @param graphicDownload
	 */
	public void updateGraphicDownload(GraphicDownload graphicDownload) {
		GraphicDownload originalGraphicDownload = graphicTemplate.loadGraphicDownload(graphicDownload.getDownloadId());
		if (originalGraphicDownload == null) {
			throw new RepositoryException(String.format("图表下载任务不能为空"));
		}
		graphicDownload.validateGraphicDownload();
		graphicTemplate.updateGraphicDownload(graphicDownload);
	}
	
	/**
	 * 删除图表下载任务
	 * @param downloadId
	 */
	public void deleteGraphicDownload(long downloadId) {
		GraphicDownload graphicDownload = graphicTemplate.findGraphicDownload(downloadId);
		if (graphicDownload == null) {
			throw new RepositoryException(String.format("图表下载任务不存在"));
		}
		
		File file = new File(graphicDownload.getFileUrl());
		if (file.exists()) {
			if (!file.isFile()) {
				throw new RepositoryException("下载文件路径非文件");
			}
			if (!file.delete()) {
				throw new RepositoryException("删除下载文件失败");
			}
		}
		graphicTemplate.deleteGraphicDownload(downloadId);
	}
	
	/**
	 * 图表下载任务不存在
	 * @param downloadId
	 * @return
	 */
	public GraphicDownload findGraphicDownload(long downloadId) {
		GraphicDownload graphicDownload = graphicTemplate.findGraphicDownload(downloadId);
		if (graphicDownload == null) {
			throw new RepositoryException(String.format("图表下载任务不存在"));
		}
		return graphicDownload;
	}
	
	/**
	 * 查询等待图表下载任务
	 * @param pageSize
	 * @return
	 */
	public List<GraphicDownload> queryPageGraphicDownloads(String founderId, String fileName, GraphicType graphicType, DownloadStatus status, int pageNum, int pageSize) {
		return graphicTemplate.queryPageGraphicDownloads(founderId, fileName, graphicType, status, OrderType.desc, pageNum, pageSize);
	}
	
	/**
	 * 查询图表导出列表
	 * @param pageSize
	 * @return
	 */
	public List<GraphicDownload> queryWaitGraphicDownloads(int pageSize) {
		return graphicTemplate.queryPageGraphicDownloads(null, null, null, DownloadStatus.download_wait, OrderType.asc, 0, pageSize);
	}
}
