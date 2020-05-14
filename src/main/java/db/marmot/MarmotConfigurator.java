package db.marmot;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import db.marmot.graphic.GraphicRepository;
import db.marmot.graphic.contorller.DashboardControllerAdapter;
import db.marmot.graphic.contorller.GraphicDataControllerAdapter;
import db.marmot.graphic.contorller.GraphicDownloadControllerAdapter;
import db.marmot.graphic.download.GraphicDataDownloadAdapter;
import db.marmot.graphic.download.GraphicDownloadAdapter;
import db.marmot.graphic.download.GraphicDownloadListener;
import db.marmot.graphic.generator.GraphicDataGeneratorAdapter;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import db.marmot.repository.DataSourceRepositoryAdapter;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.StatisticalDataGenerateAdapter;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.VolumeRepository;
import db.marmot.volume.controller.VolumeControllerAdapter;
import db.marmot.volume.generator.ColumnDataGeneratorAdapter;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

/**
 * @author shaokang
 */
public class MarmotConfigurator implements ApplicationContextAware, InitializingBean {
	
	/**
	 * 是否分表
	 */
	private boolean sharding = false;
	/**
	 * 模型数-统计模型线程最大线程数
	 */
	private int modelThreadSize = 50;
	/**
	 * 文件路径
	 */
	private String fileUrl;
	/**
	 * 文件下载线程数
	 */
	private int downloadThreadSize = 5;
	/**
	 * 下载文件地址
	 */
	private String downloadUrl;
	
	private ApplicationContext applicationContext;
	
	public void setSharding(boolean sharding) {
		this.sharding = sharding;
	}
	
	public void setModelThreadSize(int modelThreadSize) {
		this.modelThreadSize = modelThreadSize;
	}
	
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	
	public void setDownloadThreadSize(int downloadThreadSize) {
		this.downloadThreadSize = downloadThreadSize;
	}
	
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void afterPropertiesSet() {
		Validators.notBlank(this.fileUrl, "fileUrl 不能为空");
		Validators.notBlank(this.downloadUrl, "downloadUrl 不能为空");
		Validators.isTrue(this.modelThreadSize > 0, "modelThreadSize 必须大于零");
		Validators.isTrue(this.downloadThreadSize > 0, "downloadThreadSize 必须大于零");
		
		registerBean();
	}
	
	private void registerBean() {
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		registerDataSourceRepositoryAdapter(factory);
		registerColumnGeneratorAdapter(factory);
		registerStatisticalGenerateAdapter(factory);
		registerGraphicGeneratorAdapter(factory);
		registerGraphicDownloadAdapter(factory);
		registerVolumeControllerAdapter(factory);
		registerDashboardControllerAdapter(factory);
		registerGraphicDataControllerAdapter(factory);
		registerGraphicDownloadControllerAdapter(factory);
	}
	
	private void registerDataSourceRepositoryAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(DataSourceRepositoryAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("sharding", sharding);
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("repositoryAdapter", beanDefinition);
		factory.getBean(DataSourceRepositoryAdapter.class);
	}
	
	private void registerColumnGeneratorAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(ColumnDataGeneratorAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("repositoryAdapter", applicationContext.getBean(RepositoryAdapter.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("columnGeneratorAdapter", beanDefinition);
		factory.getBean(ColumnDataGeneratorAdapter.class);
	}
	
	private void registerStatisticalGenerateAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(StatisticalDataGenerateAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("maxPoolSize", modelThreadSize);
		repositoryBeanPropertyValues.add("repositoryAdapter", applicationContext.getBean(RepositoryAdapter.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("statisticalGenerateAdapter", beanDefinition);
		factory.getBean(StatisticalDataGenerateAdapter.class);
	}
	
	private void registerGraphicGeneratorAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(GraphicDataGeneratorAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("columnGeneratorAdapter", applicationContext.getBean(ColumnGeneratorAdapter.class));
		repositoryBeanPropertyValues.add("statisticalGenerateAdapter", applicationContext.getBean(StatisticalGenerateAdapter.class));
		repositoryBeanPropertyValues.add("repositoryAdapter", applicationContext.getBean(RepositoryAdapter.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("statisticalGenerateAdapter", beanDefinition);
		factory.getBean(GraphicDataGeneratorAdapter.class);
	}
	
	private void registerGraphicDownloadAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(GraphicDataDownloadAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("fileUrl", fileUrl);
		repositoryBeanPropertyValues.add("downloadNum", downloadThreadSize);
		repositoryBeanPropertyValues.add("downloadUrl", downloadUrl);
		try {
			repositoryBeanPropertyValues.add("graphicDownloadListener", applicationContext.getBean(GraphicDownloadListener.class));
		} catch (BeansException e) {
			//-nothing
		}
		repositoryBeanPropertyValues.add("repositoryAdapter", applicationContext.getBean(RepositoryAdapter.class));
		repositoryBeanPropertyValues.add("graphicGeneratorAdapter", applicationContext.getBean(GraphicGeneratorAdapter.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("graphicDownloadAdapter", beanDefinition);
		factory.getBean(GraphicDataDownloadAdapter.class);
	}
	
	private void registerVolumeControllerAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(VolumeControllerAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("columnGeneratorAdapter", applicationContext.getBean(ColumnGeneratorAdapter.class));
		repositoryBeanPropertyValues.add("volumeRepository", applicationContext.getBean(VolumeRepository.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("volumeControllerAdapter", beanDefinition);
		factory.getBean(VolumeControllerAdapter.class);
	}
	
	private void registerDashboardControllerAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(DashboardControllerAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("graphicRepository", applicationContext.getBean(GraphicRepository.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("dashboardControllerAdapter", beanDefinition);
		factory.getBean(DashboardControllerAdapter.class);
	}
	
	private void registerGraphicDataControllerAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(GraphicDataControllerAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("graphicDownloadAdapter", applicationContext.getBean(GraphicDownloadAdapter.class));
		repositoryBeanPropertyValues.add("graphicGeneratorAdapter", applicationContext.getBean(GraphicGeneratorAdapter.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("graphicDataControllerAdapter", beanDefinition);
		factory.getBean(GraphicDataControllerAdapter.class);
	}
	
	private void registerGraphicDownloadControllerAdapter(DefaultListableBeanFactory factory) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(GraphicDownloadControllerAdapter.class);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("graphicRepository", applicationContext.getBean(GraphicRepository.class));
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		factory.registerBeanDefinition("graphicDownloadControllerAdapter", beanDefinition);
		factory.getBean(GraphicDownloadControllerAdapter.class);
	}
}
