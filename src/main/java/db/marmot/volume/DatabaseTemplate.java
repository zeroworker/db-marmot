package db.marmot.volume;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.sql.builder.SQLBuilderFactory;
import com.alibaba.druid.sql.builder.SQLSelectBuilder;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.converter.ColumnConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.repository.RepositoryException;
import db.marmot.repository.validate.Validators;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class DatabaseTemplate {
	
	protected String dbType;
	protected JdbcTemplate jdbcTemplate;
	protected ConverterAdapter converterAdapter;
	protected NamedParameterJdbcTemplate parameterJdbcTemplate;
	private Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();//数据源
	
	public DatabaseTemplate(DataSource dataSource) {
		Validators.notNull(dataSource, "dataSource 不能为空");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.converterAdapter = ConverterAdapter.getInstance();
		this.parameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		Database database = new Database(dataSource);
		this.dbType = database.getDbType();
		addJdbcTemplate(database.getName(), this.jdbcTemplate);
	}
	
	public void addJdbcTemplate(String dbName, JdbcTemplate jdbcTemplate) {
		removeJdbcTemplate(dbName);
		jdbcTemplates.put(dbName, jdbcTemplate);
	}
	
	public void removeJdbcTemplate(String dbName) {
		if (jdbcTemplates.containsKey(dbName)) {
			DruidDataSource dataSource = ((DruidDataSource) jdbcTemplates.get(dbName).getDataSource());
			if (!dataSource.isRemoveAbandoned()) {
				dataSource.setRemoveAbandoned(true);
			}
			jdbcTemplates.remove(dbName);
		}
	}
	
	private JdbcTemplate getJdbcTemplate(String dbName) {
		JdbcTemplate jdbcTemplate = jdbcTemplates.get(dbName);
		if (jdbcTemplate == null) {
			throw new RepositoryException(String.format("数据源%s配置不存在", dbName));
		}
		return jdbcTemplate;
	}
	
	/**
	 * 根据表名获取当前数据源表所有字段
	 * @param dbName 表名
	 * @param tableName 表名
	 * @return 表对应字段信息
	 */
	public List<TableColumn> getTableColumns(String dbName, String tableName) {
		return getJdbcTemplate(dbName).execute(new ConnectionCallback<List<TableColumn>>() {
			
			public List<TableColumn> doInConnection(Connection con) throws SQLException, DataAccessException {
				List<TableColumn> tableColumns = Lists.newArrayList();
				DatabaseMetaData databaseMetaData = con.getMetaData();
				ResultSet rs = databaseMetaData.getColumns(null, null, tableName, null);
				try {
					while (rs.next()) {
						TableColumn tableColumn = new TableColumn();
						tableColumn.setTableName(rs.getString("REMARKS"));
						tableColumn.setColumnCode(rs.getString("COLUMN_NAME"));
						/*
						 * 字段描述信息无法获取 设置connection属性
						 * props.setProperty("remarks", "true"); //设置可以获取remarks信息
						 */
						tableColumn.setContent(rs.getString("REMARKS"));
						tableColumns.add(tableColumn);
					}
				} finally {
					JdbcUtils.close(rs);
				}
				return tableColumns;
			}
		});
	}
	
	/**
	 * 根据查询sql获取该sql所有的字段
	 * @param dbName 数据库名称
	 * @param volumeCode 数据集编码
	 * @param sqlScript sql脚本 sql 不支持子查询 子查询无法获取字段对应的表格信息
	 * @return 数据字段
	 */
	public List<DataColumn> getDataColumns(String dbName, String volumeCode, String sqlScript) {
		SQLSelectBuilder sqlSelectBuilder = SQLBuilderFactory.createSelectSQLBuilder(sqlScript, dbType).limit(1);
		return getJdbcTemplate(dbName).query(sqlSelectBuilder.toString(), new ResultSetExtractor<List<DataColumn>>() {
			
			public List<DataColumn> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<DataColumn> dataColumns = Lists.newArrayList();
				ResultSetMetaData resultVolumeMetaData = rs.getMetaData();
				int columnCount = resultVolumeMetaData.getColumnCount();
				for (int index = 1; index <= columnCount; index++) {
					DataColumn dataColumn = new DataColumn();
					dataColumn.setVolumeCode(volumeCode);
					dataColumn.setColumnOrder(index);
					dataColumn.setColumnIndex(resultVolumeMetaData.isAutoIncrement(index));
					dataColumn.setColumnCode(resultVolumeMetaData.getColumnLabel(index));
					dataColumn.setScreenColumn(resultVolumeMetaData.getColumnLabel(index));
					ColumnConverter columnConverter = ConverterAdapter.getInstance().getColumnConverter(resultVolumeMetaData.getColumnType(index));
					dataColumn.setColumnType(columnConverter.columnType());
					dataColumn.setDataFormat(columnConverter.defaultDataFormat());
					List<TableColumn> tableColumns = getTableColumns(dbName, resultVolumeMetaData.getTableName(index));
					for (TableColumn tableColumn : tableColumns) {
						if (tableColumn.getColumnCode().equals(resultVolumeMetaData.getColumnName(index))) {
							dataColumn.setContent(tableColumn.getContent());
							dataColumn.setColumnName(tableColumn.getContent());
							break;
						}
					}
					if (!dataColumns.add(dataColumn)) {
						throw new RepositoryException(String.format("存在相同的数据集字段：%s", dataColumn.getColumnCode()));
					}
				}
				return dataColumns;
			}
			
		});
	}
	
	/**
	 * 获取数据范围
	 * @param dbName
	 * @param sqlScript
	 * @return
	 */
	public DataRange getDataRange(String dbName, String sqlScript) {
		return DataAccessUtils.uniqueResult(getJdbcTemplate(dbName).query(sqlScript, new RowMapper<DataRange>() {
			public DataRange mapRow(ResultSet rs, int rowNum) throws SQLException {
				DataRange dataRange = new DataRange();
				dataRange.setMinValue(rs.getLong(1));
				dataRange.setMaxValue(rs.getLong(2));
				return dataRange;
			}
		}));
	}
	
	/**
	 * 获取数据
	 * @param dbName
	 * @param sqlScript
	 * @return
	 */
	public List<Map<String, Object>> queryData(String dbName, String sqlScript) {
		return getJdbcTemplate(dbName).query(sqlScript, new RowMapper<Map<String, Object>>() {
			public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String, Object> rowData = Maps.newLinkedHashMap();
				ResultSetMetaData metaData = rs.getMetaData();
				for (int index = 1; index <= metaData.getColumnCount(); index++) {
					String columnCode = metaData.getColumnLabel(index);
					ColumnConverter columnConverter = converterAdapter.getColumnConverter(metaData.getColumnType(index));
					rowData.put(columnCode, columnConverter.columnValueConvert(rs, index));
				}
				return rowData;
			}
		});
	}
	
	private static final String DATABASE_STORE_SQL = "insert into marmot_database(name, db_type, url, user_name, password) values (?,?,?,?,?)";
	
	/**
	 * 存储数据库配置
	 * @param database
	 */
	public void storeDatabase(Database database) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(DATABASE_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				setDatabasePreparedStatement(ps, database);
				return ps;
			}
		}, keyHolder);
		database.setId(keyHolder.getKey().longValue());
	}
	
	private static final String DATABASE_UPDATE_SQL = "update marmot_database set name=?,db_type=?,url=?,user_name=?,password=? where id =?";
	
	/**
	 * 更新数据源
	 * @param database
	 */
	public void updateDatabase(Database database) {
		jdbcTemplate.update(DATABASE_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				setDatabasePreparedStatement(ps, database);
				ps.setLong(6, database.getId());
			}
		});
	}
	
	private void setDatabasePreparedStatement(PreparedStatement ps, Database database) throws SQLException {
		ps.setString(1, database.getName());
		ps.setString(2, database.getDbType());
		ps.setString(3, database.getUrl());
		ps.setString(4, database.getUserName());
		ps.setString(5, database.getPassword());
	}
	
	private static final String DATABASE_DELETE_SQL = "delete from marmot_database where id=?";
	
	/**
	 * 删除数据库
	 * @param id
	 */
	public void deleteDatabase(long id) {
		jdbcTemplate.update(DATABASE_DELETE_SQL, new Object[] { id });
	}
	
	private static final String DATABASE_FIND_NAME_SQL = "select id, name, db_type, url, user_name, password from marmot_database where name = ?";
	
	/**
	 * 根据名称获取数据库配置
	 * @param name
	 * @return
	 */
	public Database findDatabase(String name) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(DATABASE_FIND_NAME_SQL, new Object[] { name }, new RowMapper<Database>() {
			@Override
			public Database mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDatabase(rs);
			}
		}));
	}
	
	private static final String DATABASE_FIND_ID_SQL = "select id, name, db_type, url, user_name, password from marmot_database where id = ?";
	
	/**
	 * 根据id获取数据库配置
	 * @param id
	 * @return
	 */
	public Database findDatabase(long id) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(DATABASE_FIND_ID_SQL, new Object[] { id }, new RowMapper<Database>() {
			@Override
			public Database mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDatabase(rs);
			}
		}));
	}
	
	private static final String DATABASE_GET_SQL = "select id, name, db_type, url, user_name, password from marmot_database";
	
	/**
	 * 获取所有的数据库配置
	 * @return
	 */
	public List<Database> getDatabases() {
		return jdbcTemplate.query(DATABASE_GET_SQL, new RowMapper<Database>() {
			@Override
			public Database mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDatabase(rs);
			}
		});
	}
	
	private Database buildDatabase(ResultSet rs) throws SQLException {
		Database database = new Database();
		database.setId(rs.getLong(1));
		database.setName(rs.getString(2));
		database.setDbType(rs.getString(3));
		database.setUrl(rs.getString(4));
		database.setUserName(rs.getString(5));
		database.setPassword(rs.getString(6));
		return database;
	}
}
