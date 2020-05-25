package db.marmot.volume;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;
import db.marmot.enums.OrderType;
import db.marmot.enums.VolumeType;
import db.marmot.repository.DataSourceTemplate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.List;

/**
 * @author shaokang
 */
public class VolumeTemplate implements DataSourceTemplate {
	
	private String dbType;
	private JdbcTemplate jdbcTemplate;
	private ConverterAdapter converterAdapter;
	
	public VolumeTemplate(String dbType, JdbcTemplate jdbcTemplate) {
		this.dbType = dbType;
		this.jdbcTemplate = jdbcTemplate;
		converterAdapter = ConverterAdapter.getInstance();
	}
	
	private static final String DATA_VOLUME_STORE_SQL = "INSERT INTO marmot_data_volume (volume_name,volume_code,volume_type,db_name,sql_script,volume_limit,content) VALUES(?,?,?,?,?,?,?)";
	
	/**
	 * 保存数据集
	 * @param dataVolume 数据集配置
	 */
	public void storeDataVolume(DataVolume dataVolume) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(DATA_VOLUME_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, dataVolume.getVolumeName());
				ps.setString(2, dataVolume.getVolumeCode());
				ps.setString(3, dataVolume.getVolumeType().getCode());
				ps.setString(4, dataVolume.getDbName());
				ps.setString(5, dataVolume.getSqlScript());
				ps.setLong(6, dataVolume.getVolumeLimit());
				ps.setString(7, dataVolume.getContent());
				return ps;
			}
		}, keyHolder);
		dataVolume.setVolumeId(keyHolder.getKey().longValue());
	}
	
	private static final String DATA_VOLUME_UPDATE_SQL = "UPDATE marmot_data_volume SET volume_name =?,volume_type=?,db_name=?,sql_script=?,volume_limit=?,content =? where volume_code =?";
	
	/**
	 * 更新数据集
	 * @param dataVolume 数据集配置
	 */
	public void updateDataVolume(DataVolume dataVolume) {
		jdbcTemplate.update(DATA_VOLUME_UPDATE_SQL, new PreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, dataVolume.getVolumeName());
				ps.setString(2, dataVolume.getVolumeType().getCode());
				ps.setString(3, dataVolume.getDbName());
				ps.setString(4, dataVolume.getSqlScript());
				ps.setLong(5, dataVolume.getVolumeLimit());
				ps.setString(6, dataVolume.getContent());
				ps.setString(7, dataVolume.getVolumeCode());
			}
		});
	}
	
	private static final String DATA_VOLUME_FIND_CODE_SQL = "select volume_id, volume_name, volume_code, volume_type,db_name, sql_script,volume_limit,content from marmot_data_volume where volume_code =?";
	
	/**
	 * 根据数据集ID获取数据集
	 * @param volumeCode 数据集编码
	 * @return
	 */
	public DataVolume findDataVolume(String volumeCode) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(DATA_VOLUME_FIND_CODE_SQL, new Object[] { volumeCode }, new RowMapper<DataVolume>() {
			
			public DataVolume mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDataVolume(rs);
			}
		}));
	}
	
	/**
	 * 根据数据集名称查询数据集 若name 为空 默认查询所有;支持模糊查询
	 * @param volumeName 数据集名称
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return
	 */
	public List<DataVolume> queryPageDataVolume(String volumeName, int pageNum, int pageSize) {
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType,
			"SELECT volume_code,volume_name, volume_code,volume_type,db_name,sql_script,content FROM marmot_data_volume");
		sqlBuilder.addCondition(Operators.like, ColumnType.string, "volume_name", volumeName).addLimit(pageNum, pageSize);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<DataVolume>() {
			
			public DataVolume mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDataVolume(rs);
			}
		});
	}
	
	private DataVolume buildDataVolume(ResultSet rs) throws SQLException {
		DataVolume dataVolume = new DataVolume();
		dataVolume.setVolumeId(rs.getLong(1));
		dataVolume.setVolumeName(rs.getString(2));
		dataVolume.setVolumeCode(rs.getString(3));
		dataVolume.setVolumeType(VolumeType.getByCode(rs.getString(4)));
		dataVolume.setDbName(rs.getString(5));
		dataVolume.setSqlScript(rs.getString(6));
		dataVolume.setVolumeLimit(rs.getLong(7));
		dataVolume.setContent(rs.getString(8));
		return dataVolume;
	}
	
	private static final String DATA_COLUMN_STORE_SQL = "INSERT INTO marmot_data_column (volume_code, column_order,column_name,column_code,column_type,column_label,screen_column,column_filter,column_hidden,column_escape,column_mask,column_index,data_format,unit_value,content) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	/**
	 * 保存数据集字段
	 * @param dataColumns 数据集配置
	 */
	public void storeDataColumn(List<DataColumn> dataColumns) {
		jdbcTemplate.batchUpdate(DATA_COLUMN_STORE_SQL, new BatchPreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				DataColumn dataColumn = dataColumns.get(i);
				ps.setString(1, dataColumn.getVolumeCode());
				ps.setInt(2, dataColumn.getColumnOrder());
				ps.setString(3, dataColumn.getColumnName());
				ps.setString(4, dataColumn.getColumnCode());
				ps.setString(5, dataColumn.getColumnType().getCode());
				ps.setString(6, dataColumn.getColumnLabel());
				ps.setString(7, dataColumn.getScreenColumn());
				ps.setBoolean(8, dataColumn.isColumnFilter());
				ps.setBoolean(9, dataColumn.isColumnHidden());
				ps.setBoolean(10, dataColumn.isColumnEscape());
				ps.setBoolean(11, dataColumn.isColumnMask());
				ps.setBoolean(12, dataColumn.isColumnIndex());
				ps.setString(13, dataColumn.getDataFormat());
				ps.setDouble(14, dataColumn.getUnitValue());
				ps.setString(15, dataColumn.getContent());
			}
			
			public int getBatchSize() {
				return dataColumns.size();
			}
		});
	}
	
	private static final String DATA_COLUMN_FIND_SQL = "SELECT column_id, volume_code, column_order, column_name, column_code, column_type,column_label,screen_column,column_filter,column_hidden,column_escape,column_mask,column_index,data_format, unit_value, content FROM marmot_data_column where column_id=?";
	
	/**
	 * 根据字段ID查询数据集字段
	 * @param columnId 字段ID
	 * @return
	 */
	public DataColumn findDataColumn(long columnId) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(DATA_COLUMN_FIND_SQL, new Object[] { columnId }, new RowMapper<DataColumn>() {
			
			public DataColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDataColumn(rs);
			}
		}));
	}
	
	private static final String DATA_COLUMN_VOLUME_ID_AND_COLUMN_CODE_FIND_SQL = "SELECT column_id, volume_code, column_order, column_name, column_code, column_type,screen_column,column_filter,column_hidden,column_escape,column_mask,column_index, data_format, unit_value, content FROM marmot_data_column where volume_code=? and column_code=?";
	
	/**
	 * 根据数据集编码以及字段编码查询数据集字段
	 * @param volumeCode 数据集编码
	 * @param columnCode 字段编码
	 * @return
	 */
	public DataColumn findDataColumn(String volumeCode, String columnCode) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(DATA_COLUMN_VOLUME_ID_AND_COLUMN_CODE_FIND_SQL, new Object[] { volumeCode, columnCode }, new RowMapper<DataColumn>() {
			
			public DataColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDataColumn(rs);
			}
		}));
	}
	
	private static final String DATA_COLUMN_QUERY_SQL = "SELECT column_id, volume_code, column_order, column_name, column_code, column_type,screen_column,column_filter,column_hidden,column_escape,column_mask,column_index, data_format, unit_value, content FROM marmot_data_column where volume_code=?";
	
	/**
	 * 根据数据集编码查询数据集字段
	 * @param volumeCode 数据集编码
	 * @return
	 */
	public List<DataColumn> queryDataColumn(String volumeCode) {
		return jdbcTemplate.query(DATA_COLUMN_QUERY_SQL, new Object[] { volumeCode }, new RowMapper<DataColumn>() {
			
			public DataColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDataColumn(rs);
			}
		});
	}
	
	private DataColumn buildDataColumn(ResultSet rs) throws SQLException {
		DataColumn dataColumn = new DataColumn();
		dataColumn.setColumnId(rs.getLong(1));
		dataColumn.setVolumeCode(rs.getString(2));
		dataColumn.setColumnOrder(rs.getInt(3));
		dataColumn.setColumnName(rs.getString(4));
		dataColumn.setColumnCode(rs.getString(5));
		dataColumn.setColumnType(ColumnType.getByCode(rs.getString(6)));
		dataColumn.setColumnLabel(rs.getString(7));
		dataColumn.setScreenColumn(rs.getString(8));
		dataColumn.setColumnFilter(rs.getBoolean(9));
		dataColumn.setColumnHidden(rs.getBoolean(10));
		dataColumn.setColumnEscape(rs.getBoolean(11));
		dataColumn.setColumnMask(rs.getBoolean(12));
		dataColumn.setColumnIndex(rs.getBoolean(13));
		dataColumn.setDataFormat(rs.getString(14));
		dataColumn.setUnitValue(rs.getDouble(15));
		dataColumn.setContent(rs.getString(16));
		return dataColumn;
	}
	
	private static final String DATA_COLUMN_VOLUME_ID_DELETE_SQL = "delete from marmot_data_column where volume_code =?";
	
	/**
	 * 根据数据集编码删除数据字段
	 * @param volumeCode 数据集编码
	 * @return
	 */
	public void deleteDataColumnByVolumeCode(String volumeCode) {
		jdbcTemplate.update(DATA_COLUMN_VOLUME_ID_DELETE_SQL, new Object[] { volumeCode });
	}
	
	private static final String COLUMN_VOLUME_STORE_SQL = "INSERT INTO marmot_column_volume (volume_name,volume_code,volume_type, column_code,db_name, column_value_code, column_show_code, script, content) VALUES(?,?,?,?,?,?,?,?,?)";
	
	/**
	 * 保存字段数据集
	 * @param columnVolume 数据集配置
	 */
	public void storeColumnVolume(ColumnVolume columnVolume) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(COLUMN_VOLUME_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, columnVolume.getVolumeName());
				ps.setString(1, columnVolume.getVolumeCode());
				ps.setString(3, columnVolume.getVolumeType().getCode());
				ps.setString(4, columnVolume.getColumnCode());
				ps.setString(5, columnVolume.getDbName());
				ps.setString(6, columnVolume.getColumnValueCode());
				ps.setString(7, columnVolume.getColumnShowCode());
				ps.setString(8, columnVolume.getScript());
				ps.setString(9, columnVolume.getContent());
				return ps;
			}
		}, keyHolder);
		columnVolume.setVolumeId(keyHolder.getKey().longValue());
	}
	
	private static final String COLUMN_VOLUME_UPDATE_SQL = "UPDATE marmot_column_volume SET volume_name=?,volume_type =?,column_code=?,db_name=?,column_value_code=?,column_show_code=?,script=?,content=? where volume_code=?";
	
	/**
	 * 更新字段数据集
	 * @param columnVolume 数据集配置
	 */
	public void updateColumnVolume(ColumnVolume columnVolume) {
		jdbcTemplate.update(COLUMN_VOLUME_UPDATE_SQL, new PreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, columnVolume.getVolumeName());
				ps.setString(2, columnVolume.getVolumeType().getCode());
				ps.setString(3, columnVolume.getColumnCode());
				ps.setString(4, columnVolume.getDbName());
				ps.setString(5, columnVolume.getColumnValueCode());
				ps.setString(6, columnVolume.getColumnShowCode());
				ps.setString(7, columnVolume.getScript());
				ps.setString(8, columnVolume.getContent());
				ps.setString(9, columnVolume.getVolumeCode());
			}
		});
	}
	
	private static final String COLUMN_VOLUME_DELETE_SQL = "delete from marmot_column_volume where volume_id =?";
	
	/**
	 * 根据数据集ID删除字段数据集
	 * @param volumeId 数据集ID
	 */
	public void deleteColumnVolume(long volumeId) {
		jdbcTemplate.update(COLUMN_VOLUME_DELETE_SQL, new Object[] { volumeId });
	}
	
	public static final String COLUMN_VOLUME_FIND_VOLUME_ID__SQL = "SELECT volume_id,volume_name,volume_code, volume_type, column_code,db_name, column_value_code, column_show_code,script, content FROM marmot_column_volume where volume_id =?";
	
	/**
	 * 根据数据集ID查询字段数据集
	 * @param volumeId 数据集ID
	 * @return
	 */
	public ColumnVolume findColumnVolume(long volumeId) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(COLUMN_VOLUME_FIND_VOLUME_ID__SQL, new Object[] { volumeId }, new RowMapper<ColumnVolume>() {
			
			public ColumnVolume mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildColumnVolume(rs);
			}
		}));
	}
	
	public static final String COLUMN_VOLUME_FIND_COLUMN_CODE_SQL = "SELECT volume_id,volume_name,volume_code, volume_type, column_code, db_name,column_value_code, column_show_code,script, content FROM marmot_column_volume where column_code =?";
	
	/**
	 * 根据字段编码查询字段数据集
	 * @param columnCode 字段编码
	 * @return
	 */
	public ColumnVolume findColumnVolume(String columnCode) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(COLUMN_VOLUME_FIND_COLUMN_CODE_SQL, new Object[] { columnCode }, new RowMapper<ColumnVolume>() {
			
			public ColumnVolume mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildColumnVolume(rs);
			}
		}));
	}
	
	/**
	 * 查询字段数据集
	 * @param columnCode 字段编码
	 * @param volumeType 数据集类型
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return
	 */
	public List<ColumnVolume> queryPageColumnVolume(String columnCode, String volumeType, int pageNum, int pageSize) {
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType,
			"SELECT volume_id,volume_name,volume_code, volume_type, column_code, column_value_code, db_name,column_show_code, script, content FROM marmot_column_volume");
		sqlBuilder.addCondition(Operators.like, ColumnType.string, "column_code", columnCode).addCondition(Operators.equals, ColumnType.string, "volume_type", volumeType)
			.addOrderBy("volume_id", OrderType.desc).addLimit(pageNum, pageSize);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<ColumnVolume>() {
			
			public ColumnVolume mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildColumnVolume(rs);
			}
		});
	}
	
	private ColumnVolume buildColumnVolume(ResultSet rs) throws SQLException {
		ColumnVolume columnVolume = new ColumnVolume();
		columnVolume.setVolumeId(rs.getLong(1));
		columnVolume.setVolumeName(rs.getString(2));
		columnVolume.setColumnCode(rs.getString(3));
		columnVolume.setVolumeType(VolumeType.getByCode(rs.getString(4)));
		columnVolume.setColumnCode(rs.getString(5));
		columnVolume.setColumnValueCode(rs.getString(6));
		columnVolume.setColumnValueCode(rs.getString(7));
		columnVolume.setScript(rs.getString(8));
		columnVolume.setContent(rs.getString(9));
		return columnVolume;
	}
}
