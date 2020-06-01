package db.marmot.volume;

import com.alibaba.druid.pool.DruidDataSource;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import db.marmot.repository.validate.Validators;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class DataBaseRepository extends VolumeRepository implements InitializingBean {
	
	public DataBaseRepository(DataSourceTemplate dataSourceTemplate) {
		super(dataSourceTemplate);
	}
	
	/**
	 * 保存数据源配置
	 * @param database
	 */
	public void storeDatabase(Database database) {
		Validators.notNull(database, "数据源不能为空");
		database.validateDatabase();
		DruidDataSource dataSource = buildDruidDataSource(database);
		try {
			dataSource.init();
			dataSourceTemplate.storeDatabase(database);
			dataSourceTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
		} catch (SQLException sqlException) {
			throw new RepositoryException(String.format("数据源%s初始化失败", database.getName()), sqlException);
		} catch (DuplicateKeyException e) {
			dataSourceTemplate.updateDatabase(database);
			dataSourceTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
		}
	}
	
	/**
	 * 查询数据库配置
	 * @param name
	 * @return
	 */
	public Database findDatabase(String name) {
		Database database = getDatabase(name);
		Validators.notNull(database, String.format("数据源%s配置不存在", name));
		return database;
	}
	
	/**
	 * 查询数据库配置
	 * @param name
	 * @return
	 */
	public Database getDatabase(String name) {
		Database database = dataSourceTemplate.findDatabase(name);
		return database;
	}
	
	/**
	 * 获取数据范围
	 * @param databaseName
	 * @param sqlScript
	 * @return
	 */
	public DataRange getDataRange(String databaseName, String sqlScript) {
		return dataSourceTemplate.getDataRange(databaseName, sqlScript);
	}
	
	/**
	 * 查询数据
	 * @param databaseName
	 * @param sqlScript
	 * @return
	 */
	public List<Map<String, Object>> querySourceData(String databaseName, String sqlScript) {
		return dataSourceTemplate.queryData(databaseName, sqlScript);
	}
	
	@Override
	public void afterPropertiesSet() {
		List<Database> databases = dataSourceTemplate.getDatabases();
		if (databases != null && !databases.isEmpty()) {
			databases.forEach(database -> {
				if (!database.getName().equals("master")) {
					DruidDataSource dataSource = buildDruidDataSource(database);
					dataSourceTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
				}
			});
		}
	}
	
	private DruidDataSource buildDruidDataSource(Database database) {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setMinIdle(20);
		dataSource.setMaxActive(200);
		dataSource.setMaxWait(10000);
		dataSource.setInitialSize(5);
		dataSource.setUrl(database.getUrl());
		dataSource.setValidationQueryTimeout(5);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setPassword(database.getPassword());
		dataSource.setUsername(database.getUserName());
		dataSource.setDriverClassLoader(ClassUtils.getDefaultClassLoader());
		return dataSource;
	}
}
