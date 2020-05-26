package db.marmot.repository;

import db.marmot.volume.CustomTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;

/**
 * @author shaokang
 */
@Slf4j
public class DataSourceRepositoryFactoryBean extends RepositoryFactoryBean<DataSourceRepository> implements ApplicationContextAware {
	
	private CustomTemplate customTemplate;
	private ApplicationContext applicationContext;
	
	public DataSourceRepositoryFactoryBean(boolean sharding, DataSource dataSource) {
		super(sharding, dataSource);
	}
	
	@Override
	public DataSourceRepository newInstance() {
		return new DataSourceRepository(new DataSourceTemplate(dataSource, customTemplate));
	}
	
	@Override
	public void afterPropertiesSet() {
		try {
			customTemplate = applicationContext.getBean(CustomTemplate.class);
		} catch (BeansException e) {
			log.warn("customTemplate 未实现,自定义数据集将无法使用");
		} finally {
			super.afterPropertiesSet();
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
