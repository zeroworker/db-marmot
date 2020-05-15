package db.marmot.volume;

import com.alibaba.druid.pool.DruidDataSource;
import db.marmot.enums.TemplateType;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
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
public class DataBaseRepository extends DataSourceRepository implements InitializingBean {
	
	private DatabaseTemplate databaseTemplate;
	
	public DataBaseRepository(Map<TemplateType, DataSourceTemplate> templates) {
		super(templates);
		this.databaseTemplate = getTemplate(TemplateType.database);
	}
	
	/**
	 * 保存数据源配置
	 * @param database
	 */
	public void storeDatabase(Database database) {
		if (database == null) {
			throw new RepositoryException("数据源不能为空");
		}
		database.validateDatabase();
		try {
			DruidDataSource dataSource = buildDruidDataSource(database);
			dataSource.init();
			databaseTemplate.storeDatabase(database);
			databaseTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
		} catch (SQLException sqlException) {
			throw new RepositoryException(String.format("数据源%s初始化失败", database.getName()), sqlException);
		} catch (DuplicateKeyException e) {
			throw new RepositoryException(String.format("重复数据源%s配置", database.getName()));
		}
	}
	
	/**
	 * 更新数据库配置
	 * @param database
	 */
	public void updateDatabase(Database database) {
		if (database == null) {
			throw new RepositoryException("数据源不能为空");
		}
		database.validateDatabase();
		
		try {
			databaseTemplate.updateDatabase(database);
			DruidDataSource dataSource = buildDruidDataSource(database);
			databaseTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
		} catch (DuplicateKeyException e) {
			throw new RepositoryException(String.format("重复数据源%s配置", database.getName()));
		}
	}
	
	/**
	 * 删除数库库配置
	 * @param id
	 */
	public void deleteDatabase(long id) {
		Database database = databaseTemplate.findDatabase(id);
		if (database == null) {
			throw new RepositoryException("数据源配置不存在");
		}
		databaseTemplate.deleteDatabase(id);
		databaseTemplate.removeJdbcTemplate(database.getName());
	}
	
	/**
	 * 查询数据库配置
	 * @param name
	 * @return
	 */
	public Database findDatabase(String name) {
		Database database = getDatabase(name);
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s配置不存在", name));
		}
		return database;
	}
	
	/**
	 * 查询数据库配置
	 * @param name
	 * @return
	 */
	public Database getDatabase(String name) {
		Database database = databaseTemplate.findDatabase(name);
		return database;
	}
	
	/**
	 * 获取数据范围
	 * @param databaseName
	 * @param sqlScript
	 * @return
	 */
	public DataRange getDataRange(String databaseName, String sqlScript) {
		return databaseTemplate.getDataRange(databaseName, sqlScript);
	}
	
	/**
	 * 查询数据
	 * @param databaseName
	 * @param sqlScript
	 * @return
	 */
	public List<Map<String, Object>> queryData(String databaseName, String sqlScript) {
		return databaseTemplate.queryData(databaseName, sqlScript);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<Database> databases = databaseTemplate.getDatabases();
		if (databases != null && !databases.isEmpty()) {
			databases.forEach(database -> {
				if (!database.getName().equals("master")) {
					DruidDataSource dataSource = buildDruidDataSource(database);
					databaseTemplate.addJdbcTemplate(database.getName(), new JdbcTemplate(dataSource));
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
