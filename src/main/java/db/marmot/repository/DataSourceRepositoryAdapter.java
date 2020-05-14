package db.marmot.repository;

import db.marmot.enums.RepositoryType;
import db.marmot.enums.TemplateType;
import db.marmot.graphic.GraphicRepository;
import db.marmot.graphic.GraphicRepositoryBean;
import db.marmot.graphic.GraphicTemplate;
import db.marmot.statistical.StatisticalRepository;
import db.marmot.statistical.StatisticalRepositoryBean;
import db.marmot.statistical.StatisticalTemplate;
import db.marmot.volume.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shaokang
 */
@Slf4j
public class DataSourceRepositoryAdapter implements RepositoryAdapter, ApplicationContextAware, InitializingBean {

	private boolean sharding;
	private ApplicationContext applicationContext;
	private Map<RepositoryType, DataSourceRepository> repositories = new HashMap<>();

	@Override
	public void setSharding(boolean sharding) {
		this.sharding = sharding;
	}

	@Override
	public <R extends DataSourceRepository> R getRepository(RepositoryType repository) {
		DataSourceRepository dataSourceRepository = repositories.get(repository);
		if (dataSourceRepository == null) {
			throw new RepositoryException(String.format("仓储%s未定义", repository.getCode()));
		}
		return (R) dataSourceRepository;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception{
		registerRepositoryBean();
	}

	private void registerRepositoryBean() throws Exception{
		DataSource dataSource = applicationContext.getBean(DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		Database database = new Database();
		database.setName("master");
		database.setUserName("unknown");
		database.setPassword("unknown");
		database.setUrl(dataSource.getConnection().getMetaData().getURL());
		Map<TemplateType, DataSourceTemplate> templates = getTemplates(database.getDbType(),jdbcTemplate);

		//-数据集
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		factory.registerBeanDefinition("volumeRepositoryBean", createRepositoryBeanDefinition(VolumeRepositoryBean.class,dataSource,templates));
		factory.getBean(VolumeRepositoryBean.class);
		repositories.put(RepositoryType.volume, applicationContext.getBean(VolumeRepository.class));
		//-图表
		factory.registerBeanDefinition("graphicRepositoryBean", createRepositoryBeanDefinition(GraphicRepositoryBean.class,dataSource,templates));
		factory.getBean(GraphicRepositoryBean.class);
		repositories.put(RepositoryType.graphic, applicationContext.getBean(GraphicRepository.class));
		//-统计
		factory.registerBeanDefinition("statisticalRepositoryBean", createRepositoryBeanDefinition(StatisticalRepositoryBean.class,dataSource,templates));
		factory.getBean(StatisticalRepositoryBean.class);
		repositories.put(RepositoryType.statistical, applicationContext.getBean(StatisticalRepository.class));
		//-数据库
		factory.registerBeanDefinition("databaseRepositoryBean", createRepositoryBeanDefinition(DatabaseRepositoryBean.class,dataSource,templates));
		factory.getBean(DatabaseRepositoryBean.class);
		DataBaseRepository dataBaseRepository = applicationContext.getBean(DataBaseRepository.class);
		if (dataBaseRepository.getDatabase("master")==null){
			dataBaseRepository.storeDatabase(database);
		}
		repositories.put(RepositoryType.database, dataBaseRepository);

		//-自定义
		repositories.put(RepositoryType.custom,new CustomRepository(templates));
	}

	private RootBeanDefinition createRepositoryBeanDefinition(Class repositoryBeanClass,DataSource dataSource,Map<TemplateType, DataSourceTemplate> templates) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(repositoryBeanClass);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		MutablePropertyValues repositoryBeanPropertyValues = new MutablePropertyValues();
		repositoryBeanPropertyValues.add("sharding", sharding);
		repositoryBeanPropertyValues.add("templates", templates);
		repositoryBeanPropertyValues.add("dataSource", dataSource);
		beanDefinition.setPropertyValues(repositoryBeanPropertyValues);
		return beanDefinition;
	}

	private Map<TemplateType, DataSourceTemplate> getTemplates(String dbType,JdbcTemplate jdbcTemplate){
		Map<TemplateType, DataSourceTemplate> templates = new HashMap<>();
		templates.put(TemplateType.volume, new VolumeTemplate(dbType,jdbcTemplate));
		templates.put(TemplateType.graphic, new GraphicTemplate(dbType,jdbcTemplate));
		templates.put(TemplateType.database, new DatabaseTemplate(dbType,jdbcTemplate));
		templates.put(TemplateType.statistical, new StatisticalTemplate(dbType,jdbcTemplate));
		try {
			templates.put(TemplateType.custom, applicationContext.getBean(CustomTemplate.class));
		}catch (BeansException e){
			//-nothing
		}
		return templates;
	}
}
