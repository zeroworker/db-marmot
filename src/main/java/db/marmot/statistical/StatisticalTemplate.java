package db.marmot.statistical;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.*;
import db.marmot.volume.VolumeTemplate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shaokang
 */
public class StatisticalTemplate extends VolumeTemplate {
	
	public StatisticalTemplate(DataSource dataSource) {
		super(dataSource);
	}
	
	private static final String STATISTICAL_MODEL_STORE_SQL = "insert into marmot_statistical_model(volume_code, model_name, running, calculated, offset_expr,window_length, window_type,window_unit, aggregate_columns, condition_columns, group_columns, direction_columns, memo, raw_update_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	/**
	 * 保存统计模型
	 * @param statisticalModel
	 */
	public void storeStatisticalModel(StatisticalModel statisticalModel) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(STATISTICAL_MODEL_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				setStatisticalModelPreparedStatement(ps, statisticalModel);
				return ps;
			}
		}, keyHolder);
		statisticalModel.setModelId(keyHolder.getKey().longValue());
	}
	
	private static final String STATISTICAL_MODEL_UPDATE_SQL = "update marmot_statistical_model set volume_code =?,model_name=?,running=?,calculated=?,offset_expr=?,window_length=?,window_type=?,window_unit=?,aggregate_columns=?,condition_columns=?,group_columns=?,direction_columns=?,memo=?,raw_update_time=? where model_id = ?";
	
	/**
	 * 更新统计模型
	 * @param statisticalModel
	 */
	public void updateStatisticalModel(StatisticalModel statisticalModel) {
		jdbcTemplate.update(STATISTICAL_MODEL_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				setStatisticalModelPreparedStatement(ps, statisticalModel);
				ps.setLong(15, statisticalModel.getModelId());
			}
		});
	}
	
	private void setStatisticalModelPreparedStatement(PreparedStatement ps, StatisticalModel statisticalModel) throws SQLException {
		ps.setString(1, statisticalModel.getVolumeCode());
		ps.setString(2, statisticalModel.getModelName());
		ps.setBoolean(3, statisticalModel.isRunning());
		ps.setBoolean(4, statisticalModel.isCalculated());
		ps.setString(5, JSONArray.toJSONString(statisticalModel.getOffsetExpr()));
		ps.setInt(6, statisticalModel.getWindowLength());
		ps.setString(7, statisticalModel.getWindowType().getCode());
		ps.setString(8, statisticalModel.getWindowUnit().getCode());
		ps.setString(9, JSONArray.toJSONString(statisticalModel.getAggregateColumns()));
		ps.setString(10, JSONArray.toJSONString(statisticalModel.getConditionColumns()));
		ps.setString(11, JSONArray.toJSONString(statisticalModel.getGroupColumns()));
		ps.setString(12, JSONArray.toJSONString(statisticalModel.getDirectionColumns()));
		ps.setString(13, statisticalModel.getMemo());
		ps.setDate(14, new Date(new java.util.Date().getTime()));
	}
	
	private static final String STATISTICAL_MODEL_LOAD_CALCULATE_SQL = "select model_id, volume_code, model_name,running, calculated, offset_expr, window_length, window_type,window_unit, aggregate_columns, condition_columns, group_columns, direction_columns, memo, raw_update_time from marmot_statistical_model where model_id=? and calculated=? for update ";
	
	/**
	 * 根据模型名称加载统计模型
	 * @param modelId
	 * @return
	 */
	public StatisticalModel loadStatisticalModel(long modelId, boolean calculated) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_MODEL_LOAD_CALCULATE_SQL, new Object[] { modelId, calculated }, new RowMapper<StatisticalModel>() {
			@Override
			public StatisticalModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalModel(rs);
			}
		}));
	}
	
	private static final String STATISTICAL_MODEL_FIND_SQL = "select model_id, volume_code, model_name,running, calculated, offset_expr,window_length, window_type,window_unit, aggregate_columns, condition_columns, group_columns, direction_columns, memo, raw_update_time from marmot_statistical_model where model_name=?";
	
	/**
	 * 根据模型名称加载统计模型
	 * @param modelName
	 * @return
	 */
	public StatisticalModel findStatisticalModel(String modelName) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_MODEL_FIND_SQL, new Object[] { modelName }, new RowMapper<StatisticalModel>() {
			@Override
			public StatisticalModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalModel(rs);
			}
		}));
	}
	
	private static final String STATISTICAL_MODEL_FIND_VOLUME_SQL = "select model_id, volume_code, model_name,running, calculated, offset_expr,window_length, window_type,window_unit, aggregate_columns, condition_columns, group_columns, direction_columns, memo, raw_update_time from marmot_statistical_model where volume_code=?";
	
	/**
	 * 根据数据集编码称加载统计模型
	 * @param volumeCode
	 * @return
	 */
	public List<StatisticalModel> findStatisticalModels(String volumeCode) {
		return jdbcTemplate.query(STATISTICAL_MODEL_FIND_VOLUME_SQL, new Object[] { volumeCode }, new RowMapper<StatisticalModel>() {
			@Override
			public StatisticalModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalModel(rs);
			}
		});
	}
	
	private static final String STATISTICAL_MODEL_FIND_STATUS_SQL = "select model_id, volume_code, model_name,running, calculated, offset_expr,window_length, window_type,window_unit, aggregate_columns, condition_columns, group_columns, direction_columns, memo, raw_update_time from marmot_statistical_model where running =? and calculated = ?";
	
	/**
	 * 获取统计模型
	 * @param running
	 * @param calculated
	 * @return
	 */
	public List<StatisticalModel> findStatisticalModelByStatus(boolean running, boolean calculated) {
		return jdbcTemplate.query(STATISTICAL_MODEL_FIND_STATUS_SQL, new Object[] { running, calculated }, new RowMapper<StatisticalModel>() {
			@Override
			public StatisticalModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalModel(rs);
			}
		});
	}
	
	private static final String STATISTICAL_MODEL_DELETE_SQL = "delete from marmot_statistical_model where model_name = ?";
	
	/**
	 * 删除统计模型
	 * @param modelName
	 */
	public void deleteStatisticalModel(String modelName) {
		jdbcTemplate.update(STATISTICAL_MODEL_DELETE_SQL, new Object[] { modelName });
	}
	
	/**
	 * 构建统计模型
	 * @param rs
	 * @return
	 */
	private StatisticalModel buildStatisticalModel(ResultSet rs) throws SQLException {
		StatisticalModel statisticalModel = new StatisticalModel();
		statisticalModel.setModelId(rs.getLong(1));
		statisticalModel.setVolumeCode(rs.getString(2));
		statisticalModel.setModelName(rs.getString(3));
		statisticalModel.setRunning(rs.getBoolean(4));
		statisticalModel.setCalculated(rs.getBoolean(5));
		statisticalModel.setOffsetExpr(JSONArray.parseArray(rs.getString(6), String.class));
		statisticalModel.setWindowLength(rs.getInt(7));
		statisticalModel.setWindowType(WindowType.getByCode(rs.getString(8)));
		statisticalModel.setWindowUnit(WindowUnit.getByCode(rs.getString(9)));
		statisticalModel.setAggregateColumns(JSONArray.parseArray(rs.getString(10), AggregateColumn.class));
		statisticalModel.setConditionColumns(JSONArray.parseArray(rs.getString(11), ConditionColumn.class));
		statisticalModel.setGroupColumns(JSONArray.parseArray(rs.getString(12), GroupColumn.class));
		statisticalModel.setDirectionColumns(JSONArray.parseArray(rs.getString(13), DirectionColumn.class));
		statisticalModel.setMemo(rs.getString(14));
		statisticalModel.setRawUpdateTime(rs.getDate(15));
		return statisticalModel;
	}
	
	private static final String STATISTICAL_DATA_STORE_SQL = "insert into marmot_statistical_data(model_name, row_key, aggregate_data, group_columns, time_unit, raw_update_time) values (?,?,?,?,?,?)";
	
	/**
	 * 保存统计数据
	 * @param statisticalData
	 * @return
	 */
	public void storeStatisticalData(StatisticalData statisticalData) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(STATISTICAL_DATA_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, statisticalData.getModelName());
				ps.setString(2, statisticalData.getRowKey());
				ps.setString(3, JSONArray.toJSONString(statisticalData.getAggregateData(), SerializerFeature.WriteClassName));
				ps.setString(4, JSONArray.toJSONString(statisticalData.getGroupColumns(), SerializerFeature.WriteClassName));
				ps.setDate(5, statisticalData.getTimeUnit() != null ? new Date(statisticalData.getTimeUnit().getTime()) : null);
				ps.setDate(6, new Date(new java.util.Date().getTime()));
				return ps;
			}
		}, keyHolder);
		statisticalData.setDataId(keyHolder.getKey().longValue());
	}
	
	private static final String STATISTICAL_DATA_UPDATE_SQL = "update marmot_statistical_data set aggregate_data =? where model_name=? and row_key=?";
	
	/**
	 * 更新统计数据
	 * @param statisticalData
	 */
	public void updateStatisticalData(StatisticalData statisticalData) {
		jdbcTemplate.update(STATISTICAL_DATA_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, JSONArray.toJSONString(statisticalData.getAggregateData()));
				ps.setString(2, statisticalData.getModelName());
				ps.setString(3, statisticalData.getRowKey());
			}
		});
	}
	
	private static final String STATISTICAL_DATA_FIND_SQL = "select data_id, model_name, row_key, aggregate_data, group_columns, time_unit, raw_update_time from marmot_statistical_data where model_name=? and row_key=?";
	
	/**
	 * 获取统计数据
	 * @param rowKey
	 * @return
	 */
	public StatisticalData findStatisticalData(String modelName, String rowKey) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_DATA_FIND_SQL, new Object[] { modelName, rowKey }, new RowMapper<StatisticalData>() {
			@Override
			public StatisticalData mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalData(rs);
			}
		}));
	}
	
	/**
	 * 获取统计数据
	 * @param modelName
	 * @param rowKeys
	 * @return
	 */
	public List<StatisticalData> findStatisticalData(String modelName, List<String> rowKeys) {
		SelectSqlBuilderConverter sqlBuilder =
				converterAdapter.newInstanceSqlBuilder(dbType,
						"select data_id, model_name, row_key, aggregate_data, group_columns, time_unit, raw_update_time from marmot_statistical_data")
				.addCondition(Operators.equals, ColumnType.string,"model_name",modelName)
				.addCondition(Operators.in,ColumnType.string,"row_key",rowKeys);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<StatisticalData>() {
			@Override
			public StatisticalData mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalData(rs);
			}
		});
	}
	
	private StatisticalData buildStatisticalData(ResultSet rs) throws SQLException {
		StatisticalData statisticalData = new StatisticalData();
		statisticalData.setDataId(rs.getLong(1));
		statisticalData.setModelName(rs.getString(2));
		statisticalData.setRowKey(rs.getString(3));
		statisticalData.setAggregateData(JSONArray.parseObject(rs.getString(4), Map.class));
		statisticalData.setGroupColumns(JSONArray.parseObject(rs.getString(5), List.class));
		statisticalData.setTimeUnit(rs.getDate(6));
		statisticalData.setRawUpdateTime(rs.getDate(7));
		return statisticalData;
	}
	
	private static final String STATISTICAL_TASK_STORE_SQL = "insert into marmot_statistical_task(model_name, scanned, start_index, end_index, raw_update_time) values (?,?,?,?,?)";
	
	/**
	 * 保存统计任务
	 * @param statisticalTask
	 */
	public void storeStatisticalTask(StatisticalTask statisticalTask) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(STATISTICAL_TASK_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, statisticalTask.getModelName());
				ps.setBoolean(2, statisticalTask.isScanned());
				ps.setLong(3, statisticalTask.getStartIndex());
				ps.setLong(4, statisticalTask.getEndIndex());
				ps.setDate(5, new Date(new java.util.Date().getTime()));
				return ps;
			}
		}, keyHolder);
		statisticalTask.setTaskId(keyHolder.getKey().longValue());
	}
	
	private static final String STATISTICAL_TASK_UPDATE_SQL = "update marmot_statistical_task set scanned=? where task_id=?";
	
	/**
	 * 更新统计任务
	 * @param statisticalTask
	 */
	public void updateStatisticalTask(StatisticalTask statisticalTask) {
		jdbcTemplate.update(STATISTICAL_TASK_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setBoolean(1, statisticalTask.isScanned());
				ps.setLong(2, statisticalTask.getTaskId());
			}
		});
	}
	
	private static final String STATISTICAL_TASK_DELETE_SQL = "delete from marmot_statistical_task where task_id=?";
	
	/**
	 * 删除统计任务
	 * @param taskId
	 */
	public void deleteStatisticalTask(long taskId) {
		jdbcTemplate.update(STATISTICAL_TASK_DELETE_SQL, new Object[] { taskId });
	}
	
	private static final String STATISTICAL_TASK_FIND_MODEL_NAME_SQL = "select task_id, model_name, scanned, start_index, end_index, raw_update_time from marmot_statistical_task where model_name=?";
	
	/**
	 * 获取统计任务
	 * @param modelName
	 * @return
	 */
	public StatisticalTask findStatisticalTask(String modelName) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_TASK_FIND_MODEL_NAME_SQL, new RowMapper<StatisticalTask>() {
			@Override
			public StatisticalTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalTask(rs);
			}
		}));
	}
	
	private static final String STATISTICAL_TASK_LOAD_SQL = "select task_id, model_name, scanned, start_index, end_index, raw_update_time from marmot_statistical_task where task_id=?";
	
	/**
	 * 获取统计任务
	 * @param taskId
	 * @return
	 */
	public StatisticalTask loadStatisticalTask(long taskId) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_TASK_LOAD_SQL, new RowMapper<StatisticalTask>() {
			@Override
			public StatisticalTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalTask(rs);
			}
		}));
	}
	
	private StatisticalTask buildStatisticalTask(ResultSet rs) throws SQLException {
		StatisticalTask statisticalTask = new StatisticalTask();
		statisticalTask.setTaskId(rs.getLong(1));
		statisticalTask.setModelName(rs.getString(2));
		statisticalTask.setScanned(rs.getBoolean(3));
		statisticalTask.setStartIndex(rs.getLong(4));
		statisticalTask.setEndIndex(rs.getLong(5));
		statisticalTask.setRawUpdateTime(rs.getDate(6));
		return statisticalTask;
	}
	
	private static final String STATISTICAL_DISTINCT_STORE_SQL = "insert into marmot_statistical_distinct(model_name, row_key, distinct_column, distinct_data) values (?,?,?,?)";
	
	/**
	 * 保存统计去重数据
	 * @param statisticalDistinct
	 */
	public void storeStatisticalDistinct(StatisticalDistinct statisticalDistinct) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(STATISTICAL_DISTINCT_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, statisticalDistinct.getModelName());
				ps.setString(2, statisticalDistinct.getRowKey());
				ps.setString(3, statisticalDistinct.getDistinctColumn());
				ps.setString(4, JSONArray.toJSONString(statisticalDistinct.getDistinctData(), SerializerFeature.WriteClassName));
				return ps;
			}
		}, keyHolder);
		statisticalDistinct.setDistinctId(keyHolder.getKey().longValue());
	}
	
	private static final String STATISTICAL_DISTINCT_UPDATE_SQL = "update marmot_statistical_distinct set distinct_data =? where distinct_id = ?";
	
	/**
	 * 更新统计去重数据
	 * @param statisticalDistinct
	 */
	public void updateStatisticalDistinct(StatisticalDistinct statisticalDistinct) {
		jdbcTemplate.update(STATISTICAL_DISTINCT_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, JSONArray.toJSONString(statisticalDistinct.getDistinctData(), SerializerFeature.WriteClassName));
				ps.setLong(2, statisticalDistinct.getDistinctId());
			}
		});
	}
	
	private static final String STATISTICAL_DISTINCT_FIND_SQL = "select distinct_id, model_name, row_key, distinct_column, distinct_data from marmot_statistical_distinct where row_key =? and distinct_column=?";
	
	/**
	 * 获取去重数据
	 * @return
	 */
	public StatisticalDistinct findStatisticalDistinct(String rowKey, String distinctColumn) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_DISTINCT_FIND_SQL, new Object[] { rowKey, distinctColumn }, new RowMapper<StatisticalDistinct>() {
			@Override
			public StatisticalDistinct mapRow(ResultSet rs, int rowNum) throws SQLException {
				StatisticalDistinct statisticalDistinct = new StatisticalDistinct();
				statisticalDistinct.setDistinctId(rs.getLong(1));
				statisticalDistinct.setModelName(rs.getString(2));
				statisticalDistinct.setRowKey(rs.getString(3));
				statisticalDistinct.setDistinctColumn(rs.getString(4));
				statisticalDistinct.setDistinctData(JSONArray.parseObject(rs.getString(5), Set.class));
				return statisticalDistinct;
			}
		}));
	}
	
	private static final String STATISTICAL_REVISE_TASK_STORE_SQL = "insert into marmot_statistical_revise_task(volume_code, revise_status,start_index,offset_index, end_index) values (?,?,?,?,?)";
	
	/**
	 * 保存统计订正任务
	 * @param reviseTask
	 */
	public void storeStatisticalReviseTask(StatisticalReviseTask reviseTask) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(STATISTICAL_REVISE_TASK_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, reviseTask.getVolumeCode());
				ps.setString(2, reviseTask.getReviseStatus().getCode());
				ps.setLong(3, reviseTask.getStartIndex());
				ps.setLong(4, reviseTask.getOffsetIndex());
				ps.setLong(5, reviseTask.getEndIndex());
				return ps;
			}
		}, keyHolder);
		reviseTask.setTaskId(keyHolder.getKey().longValue());
	}
	
	private static final String STATISTICAL_REVISE_TASK_UPDATE_SQL = "update marmot_statistical_revise_task set revise_status =?,offset_index=? where task_id = ?";
	
	/**
	 * 更新统计订正任务
	 * @param reviseTask
	 */
	public void updateStatisticalReviseTask(StatisticalReviseTask reviseTask) {
		jdbcTemplate.update(STATISTICAL_REVISE_TASK_UPDATE_SQL, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, reviseTask.getReviseStatus().getCode());
				ps.setLong(2, reviseTask.getOffsetIndex());
				ps.setLong(2, reviseTask.getTaskId());
			}
		});
	}
	
	private static final String STATISTICAL_REVISE_TASK_DELETE_SQL = "delete from marmot_statistical_revise_task where task_id = ?";
	
	/**
	 * 删除统计订正任务
	 * @param taskId
	 */
	public void deleteStatisticalReviseTask(long taskId) {
		jdbcTemplate.update(STATISTICAL_REVISE_TASK_DELETE_SQL, new Object[] { taskId });
	}
	
	private static final String STATISTICAL_REVISE_TASK_LOAD_SQL = "select task_id, volume_code, revise_status, start_index,offset_index, end_index from marmot_statistical_revise_task where volume_code=? for update";
	
	/**
	 * 加载统计订正任务
	 * @param volumeCode
	 * @return
	 */
	public StatisticalReviseTask loadStatisticalReviseTask(String volumeCode) {
		return DataAccessUtils.uniqueResult(jdbcTemplate.query(STATISTICAL_REVISE_TASK_LOAD_SQL, new Object[] { volumeCode }, new RowMapper<StatisticalReviseTask>() {
			@Override
			public StatisticalReviseTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalReviseTask(rs);
			}
		}));
	}

	/**
	 * 分页查询统计订正任务
	 * @param volumeCode
	 * @param reviseStatus
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<StatisticalReviseTask> queryPageStatisticalReviseTasks(String volumeCode,String reviseStatus,int pageNum, int pageSize){
		SelectSqlBuilderConverter sqlBuilder =
				converterAdapter.newInstanceSqlBuilder(dbType,
						"select task_id, volume_code, revise_status, start_index,offset_index, end_index from marmot_statistical_revise_task")
		.addCondition(Operators.equals,ColumnType.string,"volume_code",volumeCode)
		.addCondition(Operators.equals,ColumnType.string,"revise_status",reviseStatus)
		.addLimit(pageNum,pageSize);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<StatisticalReviseTask>() {
			@Override
			public StatisticalReviseTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildStatisticalReviseTask(rs);
			}
		});
	}

	private StatisticalReviseTask buildStatisticalReviseTask(ResultSet rs)throws SQLException{
		StatisticalReviseTask statisticalReviseTask = new StatisticalReviseTask();
		statisticalReviseTask.setTaskId(rs.getLong(1));
		statisticalReviseTask.setVolumeCode(rs.getString(2));
		statisticalReviseTask.setReviseStatus(ReviseStatus.getByCode(rs.getString(3)));
		statisticalReviseTask.setStartIndex(rs.getLong(4));
		statisticalReviseTask.setOffsetIndex(rs.getLong(5));
		statisticalReviseTask.setEndIndex(rs.getLong(6));
		return statisticalReviseTask;
	}
}
