package db.marmot.graphic.download;

import com.google.common.collect.Maps;
import db.marmot.enums.GraphicType;
import db.marmot.enums.RepositoryType;
import db.marmot.graphic.*;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.DataVolume;
import db.marmot.volume.VolumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shaokang
 */
@Slf4j
public class GraphicDataDownloadAdapter implements GraphicDownloadAdapter, ApplicationContextAware, InitializingBean, ApplicationListener<ContextClosedEvent> {
	
	private String fileUrl;
	private int downloadNum;
	private String downloadUrl;
	private VolumeRepository volumeRepository;
	private GraphicRepository graphicRepository;
	private ApplicationContext applicationContext;
	private ThreadPoolTaskExecutor downloadThreadPool;
	private GraphicDownloadListener graphicDownloadListener;
	private GraphicGeneratorAdapter graphicGeneratorAdapter;
	private Map<GraphicType, Class> graphicDownloaderClasses = Maps.newHashMap();
	private static final AtomicLong fileSeqGen = new AtomicLong(System.currentTimeMillis());
	
	public GraphicDataDownloadAdapter() {
		graphicDownloaderClasses.put(GraphicType.cross_tab, TabGraphicDownloader.class);
	}
	
	@Override
	public void setFileUrl(String fileUrl) {
		Validators.notNull(fileUrl, "fileUrl 不能为空");
		Path filePath = Paths.get(fileUrl);
		if (!Files.exists(filePath)) {
			try {
				Files.createDirectories(filePath);
			} catch (IOException e) {
				throw new GraphicDownloadException(String.format("图表下载文件存储地址创建失败", e));
			}
		}
		
		if (!fileUrl.endsWith(File.separator)) {
			fileUrl = StringUtils.join(filePath, File.separator);
		}
		
		this.fileUrl = fileUrl;
	}
	
	@Override
	public void setDownloadNum(int downloadNum) {
		Validators.isTrue(downloadNum > 0, "downloadNum 必须大于零");
		this.downloadNum = downloadNum;
	}
	
	@Override
	public void setDownloadUrl(String downloadUrl) {
		Validators.notNull(downloadUrl, "downloadUrl 不能为空");
		
		if (!downloadUrl.endsWith(File.separator)) {
			downloadUrl = StringUtils.join(downloadUrl, File.separator);
		}
		
		this.downloadUrl = downloadUrl;
	}
	
	@Override
	public void setGraphicDownloadListener(GraphicDownloadListener graphicDownloadListener) {
		Validators.notNull(graphicDownloadListener, "graphicDownloadListener 不能为空");
		this.graphicDownloadListener = graphicDownloadListener;
	}
	
	@Override
	public void setRepositoryAdapter(RepositoryAdapter repositoryAdapter) {
		Validators.notNull(repositoryAdapter, "repositoryAdapter 不能为空");
		this.graphicRepository = repositoryAdapter.getRepository(RepositoryType.graphic);
		this.volumeRepository = repositoryAdapter.getRepository(RepositoryType.volume);
	}
	
	@Override
	public void setGraphicGeneratorAdapter(GraphicGeneratorAdapter graphicGeneratorAdapter) {
		Validators.notNull(graphicGeneratorAdapter, "graphicGeneratorAdapter 不能为空");
		this.graphicGeneratorAdapter = graphicGeneratorAdapter;
	}
	
	@Override
	public GraphicDownload downloadGraphicData(String graphicCode) {
		GraphicDesign graphicDesign = graphicRepository.findGraphicDesign(graphicCode);
		Dashboard dashboard = graphicRepository.findDashboard(graphicDesign.getBoardId());
		DataVolume dataVolume = volumeRepository.findDataVolume(dashboard.getVolumeCode());
		GraphicDownload graphicDownload = buildGraphicDownload(dataVolume.getVolumeCode(), graphicDesign.getGraphicName(), graphicDesign.getGraphicType(), graphicDesign.getGraphic());
		return downloadGraphicData(graphicDownload.downloadIng());
	}
	
	@Override
	public GraphicDownload downloadGraphicData(String founderId, String graphicCode) {
		Validators.notNull(founderId, "founderId 不能为空");
		
		GraphicDesign graphicDesign = graphicRepository.findGraphicDesign(graphicCode);
		Dashboard dashboard = graphicRepository.findDashboard(graphicDesign.getBoardId());
		DataVolume dataVolume = volumeRepository.findDataVolume(dashboard.getVolumeCode());
		GraphicDownload graphicDownload = buildGraphicDownload(founderId, dataVolume.getVolumeCode(), graphicDesign.getGraphicName(), graphicDesign.getGraphicType(), graphicDesign.getGraphic());
		return downloadGraphicData(graphicDownload.downloadIng());
	}
	
	@Override
	public GraphicDownload downloadGraphicData(String volumeCode, String graphicName, GraphicType graphicType, Graphic graphic) {
		Validators.notNull(graphic, "graphic 不能为空");
		Validators.notNull(graphicName, "graphicName 不能为空");
		Validators.notNull(graphicType, "graphicType 不能为空");
		
		return downloadGraphicData(buildGraphicDownload(volumeCode, graphicName, graphicType, graphic).downloadIng());
	}
	
	@Override
	public GraphicDownload downloadGraphicData(String founderId, String volumeCode, String graphicName, GraphicType graphicType, Graphic graphic) {
		Validators.notNull(graphic, "graphic 不能为空");
		Validators.notNull(graphicName, "graphicName 不能为空");
		Validators.notNull(founderId, "founderId 不能为空");
		Validators.notNull(graphicType, "graphicType 不能为空");
		
		GraphicDownload graphicDownload = buildGraphicDownload(founderId, volumeCode, graphicName, graphicType, graphic);
		graphicRepository.storeGraphicDownload(graphicDownload);
		
		submitGraphicDownload(graphicDownload);
		return graphicDownload;
	}
	
	@Override
	public void downloadWaitGraphicData() {
		List<GraphicDownload> graphicDownloads = graphicRepository.queryWaitGraphicDownloads(downloadNum);
		if (graphicDownloads != null && graphicDownloads.size() > 0) {
			graphicDownloads.forEach(this::submitGraphicDownload);
		}
	}
	
	/**
	 * 提交图表下载任务
	 * @param graphicDownload
	 * @return
	 */
	private GraphicDownload submitGraphicDownload(GraphicDownload graphicDownload) {
		if (!graphicDownload.isDownloadWait()) {
			log.warn("图表任务非等待状态,图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType());
			return graphicDownload;
		}
		
		try {
			downloadThreadPool.submit(() -> executeGraphicDownload(graphicDownload));
		} catch (RejectedExecutionException exception) {
			log.warn("图表下载队列已满,图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType(), exception);
		} catch (Exception e) {
			log.error("图表数据下载异常 图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType(), e);
		}
		return graphicDownload;
	}
	
	/**
	 * 执行图表下载任务
	 * @param graphicDownload
	 */
	private void executeGraphicDownload(GraphicDownload graphicDownload) {
		try {
			//-1.更新图表任务为下载中状态
			graphicRepository.updateGraphicDownloadIng(graphicDownload);
			//-2. 下载图表数据
			downloadGraphicData(graphicDownload);
			//-3.更新图表下载任务结果
			if (!graphicDownload.isDownloadIng()) {
				graphicRepository.updateGraphicDownload(graphicDownload);
			}
		} catch (Exception e) {
			log.error("图表数据下载异常 图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType(), e);
		}
	}
	
	/**
	 * 图表数据下载
	 * @param graphicDownload
	 * @return
	 */
	private GraphicDownload downloadGraphicData(GraphicDownload graphicDownload) {
		
		if (!graphicDownload.isDownloadIng()) {
			throw new GraphicDownloadException("图表任务非下载中状态");
		}
		
		//-根据图表类型动态创建图表下载器
		GraphicDownloader graphicDownloader = newInstanceGraphicDownloader(graphicDownload.getGraphicType());
		log.info("图表数据开始下载 图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType());
		long timeMillis = System.currentTimeMillis();
		if (graphicDownloadListener != null) {
			graphicDownloadListener.downloadStart(graphicDownload);
		}
		
		try {
			graphicDownloader.downloadFile(graphicDownload);
			graphicDownload.downloadSuccess();
		} catch (Exception e) {
			log.error("图表数据下载异常 图表下载任务ID:{}图表名称:{},图表类型:{}", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType(), e);
			graphicDownload.downloadFail(e.getMessage());
		} finally {
			if (graphicDownloadListener != null) {
				graphicDownloadListener.downloadEnd(graphicDownload);
			}
		}
		
		log.info("图表数据结束 图表下载任务ID:{}图表名称:{},图表类型:{},下载状态:{},描述:{} 耗时:{}ms", graphicDownload.getDownloadId(), graphicDownload.getFileName(), graphicDownload.getGraphicType(),
			graphicDownload.getStatus(), graphicDownload.getMemo(), System.currentTimeMillis() - timeMillis);
		
		return graphicDownload;
	}
	
	/**
	 * 根据图表类型创建图表下载器实例
	 * @param graphicType
	 * @return
	 */
	private GraphicDownloader newInstanceGraphicDownloader(GraphicType graphicType) {
		Class graphicDownloaderClass = graphicDownloaderClasses.get(graphicType);
		if (graphicDownloaderClass == null) {
			throw new GraphicDownloadException(String.format("图表%s下载器未实现", graphicType.getCode()));
		}
		try {
			Constructor<GraphicDownloader> constructor = graphicDownloaderClass.getConstructor(GraphicGeneratorAdapter.class);
			return constructor.newInstance(graphicGeneratorAdapter);
		} catch (Exception e) {
			throw new GraphicDownloadException(String.format("创建图表%s下载器实例失败"), e);
		}
	}
	
	/**
	 * 创建图表下载任务
	 * @param volumeCode
	 * @param graphicType
	 * @param graphic
	 * @return
	 */
	private GraphicDownload buildGraphicDownload(String volumeCode, String graphicName, GraphicType graphicType, Graphic graphic) {
		return buildGraphicDownload(null, volumeCode, null, graphicName, graphicType, graphic);
	}
	/**
	 * 创建图表下载任务
	 * @param volumeCode
	 * @param graphicType
	 * @param graphic
	 * @return
	 */
	private GraphicDownload buildGraphicDownload(String volumeCode,String graphicCode, String graphicName, GraphicType graphicType, Graphic graphic) {
		return buildGraphicDownload(null, volumeCode, graphicCode, graphicName, graphicType, graphic);
	}

	/**
	 * 创建图表下载任务
	 * @param founderId
	 * @param volumeCode
	 * @param graphicType
	 * @param graphic
	 * @return
	 */
	private GraphicDownload buildGraphicDownload(String founderId, String volumeCode, String graphicCode, String graphicName, GraphicType graphicType, Graphic graphic) {
		GraphicDownload graphicDownload = new GraphicDownload();
		graphicDownload.setGraphic(graphic);
		graphicDownload.setVolumeCode(volumeCode);
		graphicDownload.setFounderId(founderId);
		graphicDownload.setGraphicCode(graphicCode);
		graphicDownload.setGraphicType(graphicType);
		graphicDownload.setFileName(StringUtils.join(graphicName, "(", fileSeqGen.incrementAndGet(), ")", ".", "xlsx"));
		graphicDownload.setFileUrl(StringUtils.join(fileUrl, graphicDownload.getFileName()));
		graphicDownload.setDownloadUrl(StringUtils.join(downloadUrl, graphicDownload.getFileName()));
		return graphicDownload;
	}
	
	@Override
	public void afterPropertiesSet() {
		Validators.notNull(fileUrl, "fileUrl 不能为空");
		Validators.notNull(downloadUrl, "downloadUrl 不能为空");
		Validators.isTrue(downloadNum > 0, "downloadNum 必须大于零");
		Validators.notNull(graphicRepository, "graphicRepository 不能为空");
		Validators.notNull(volumeRepository, "volumeRepository 不能为空");
		Validators.notNull(graphicGeneratorAdapter, "graphicGeneratorAdapter 不能为空");
		
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		RootBeanDefinition db = new RootBeanDefinition(ThreadPoolTaskExecutor.class);
		db.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues threadPoolPropertyValues = new MutablePropertyValues();
		threadPoolPropertyValues.add("maxPoolSize", downloadNum);
		threadPoolPropertyValues.add("corePoolSize", 1);
		threadPoolPropertyValues.add("queueCapacity", 0);
		threadPoolPropertyValues.add("keepAliveSeconds", 60);
		threadPoolPropertyValues.add("threadNamePrefix", "marmot-download");
		threadPoolPropertyValues.add("rejectedExecutionHandler", new ThreadPoolExecutor.DiscardPolicy());
		db.setPropertyValues(threadPoolPropertyValues);
		factory.registerBeanDefinition("marmot.download.threadPool", db);
		this.downloadThreadPool = factory.getBean("marmot.download.threadPool", ThreadPoolTaskExecutor.class);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		downloadThreadPool.shutdown();
	}
}
