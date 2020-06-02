package db.marmot.graphic;

import com.alibaba.fastjson.JSONObject;
import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.*;
import db.marmot.graphic.converter.GraphicConverter;
import db.marmot.statistical.StatisticalTemplate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * @author shaokang
 */
public class GraphicTemplate extends StatisticalTemplate {

	public GraphicTemplate(DataSource dataSource) {
		super(dataSource);
	}

	private static final String DASH_BOARD_STORE_SQL = "INSERT INTO marmot_dash_board (volume_code, board_name, board_type,founder_id, founder_name, content) VALUES(?,?,?,?,?,?)";
	
	/**
	 * 保存仪表盘
	 * @param dashboard 仪表盘
	 */
	public void storeDashboard(Dashboard dashboard) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(DASH_BOARD_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				setDashboardPreparedStatement(ps, dashboard);
				return ps;
			}
		}, keyHolder);
		dashboard.setBoardId(keyHolder.getKey().longValue());
	}
	
	private static final String DASH_BOARD_UPDATE_SQL = "UPDATE marmot_dash_board SET volume_code=?,board_name=?,board_type =?,founder_id=?,founder_name=?,content=? where board_id=?";
	
	/**
	 * 更新仪表盘
	 * @param dashboard 数据集配置
	 */
	public void updateDashboard(Dashboard dashboard) {
		jdbcTemplate.update(DASH_BOARD_UPDATE_SQL, new PreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps) throws SQLException {
				setDashboardPreparedStatement(ps, dashboard);
				ps.setLong(7, dashboard.getBoardId());
			}
		});
	}
	
	private static final String DASH_BOARD_DELETE_SQL = "delete from marmot_dash_board where board_id =?";
	
	/**
	 * 根据仪表盘ID删除仪表盘
	 * @param boardId 仪表盘ID
	 */
	public void deleteDashboard(long boardId) {
		jdbcTemplate.update(DASH_BOARD_DELETE_SQL, new Object[] { boardId });
	}
	
	private void setDashboardPreparedStatement(PreparedStatement ps, Dashboard dashboard) throws SQLException {
		ps.setString(1, dashboard.getVolumeCode());
		ps.setString(2, dashboard.getBoardName());
		ps.setString(3, dashboard.getBoardType().getCode());
		ps.setString(4, dashboard.getFounderId());
		ps.setString(5, dashboard.getFounderName());
		ps.setString(6, dashboard.getContent());
	}
	
	private static final String DASH_BOARD_FIND_SQL = "select board_id, volume_code, board_name,board_type, founder_id, founder_name, content from marmot_dash_board where board_id =?";
	
	/**
	 * 根据仪表盘ID获取仪表盘信息
	 * @param boardId 仪表盘ID
	 */
	public Dashboard findDashboard(long boardId) {
		return DataAccessUtils.singleResult(jdbcTemplate.query(DASH_BOARD_FIND_SQL, new Object[] { boardId }, new RowMapper<Dashboard>() {
			
			public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDashboard(rs);
			}
		}));
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
	public List<Dashboard> queryPageDashboard(String founderId, String boardName, String boardType, int pageNum, int pageSize) {
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType,
			"SELECT board_id, volume_code, board_name,board_type, founder_id, founder_name, content FROM marmot_dash_board");
		sqlBuilder.addCondition(Operators.equals, ColumnType.number, "founder_id", founderId).addCondition(Operators.like, ColumnType.string, "board_name", boardName)
			.addCondition(Operators.equals, ColumnType.string, "board_type", boardType).addOrderBy("board_id", OrderType.desc).addLimit(pageNum, pageSize);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<Dashboard>() {
			
			public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildDashboard(rs);
			}
		});
	}
	
	private Dashboard buildDashboard(ResultSet rs) throws SQLException {
		Dashboard dashboard = new Dashboard();
		dashboard.setBoardId(rs.getLong(1));
		dashboard.setVolumeCode(rs.getString(2));
		dashboard.setBoardName(rs.getString(3));
		dashboard.setBoardType(BoardType.getByCode(rs.getString(4)));
		dashboard.setFounderId(rs.getString(5));
		dashboard.setFounderName(rs.getString(6));
		dashboard.setContent(rs.getString(7));
		return dashboard;
	}
	
	private static final String GRAPHIC_DESIGN_STORE_SQL = "INSERT INTO marmot_graphic_design (graphic_name,graphic_code, board_id, graphic_type, graphic) VALUES(?,?,?,?,?)";
	
	/**
	 * 保存图表设计
	 * @param graphicDesign 仪表盘
	 */
	public void storeGraphicDesign(GraphicDesign graphicDesign) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(GRAPHIC_DESIGN_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, graphicDesign.getGraphicName());
				ps.setString(2, graphicDesign.getGraphicCode());
				ps.setLong(3, graphicDesign.getBoardId());
				ps.setString(4, graphicDesign.getGraphicType().getCode());
				ps.setString(5, JSONObject.toJSONString(graphicDesign.getGraphic()));
				return ps;
			}
		}, keyHolder);
		graphicDesign.setGraphicId(keyHolder.getKey().longValue());
	}
	
	private static final String GRAPHIC_DESIGN_UPDATE_SQL = "update marmot_graphic_design set board_id=?,graphic_name=?,graphic_type=?,graphic=? where graphic_id=?";
	
	/**
	 * 更新图表设计
	 * @param graphicDesign 仪表盘
	 */
	public void updateGraphicDesign(GraphicDesign graphicDesign) {
		jdbcTemplate.update(GRAPHIC_DESIGN_UPDATE_SQL, new PreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setLong(1, graphicDesign.getBoardId());
				ps.setString(2, graphicDesign.getGraphicName());
				ps.setString(3, graphicDesign.getGraphicType().getCode());
				ps.setString(4, JSONObject.toJSONString(graphicDesign.getGraphic()));
				ps.setLong(5, graphicDesign.getGraphicId());
			}
		});
	}
	
	private static final String GRAPHIC_DESIGN_BOARD_ID_DELETE_SQL = "delete from marmot_graphic_design where board_id =?";
	
	/**
	 * 根据仪表盘ID删除图表设计
	 * @param boardId 仪表盘ID
	 */
	public void deleteGraphicDesignByBoardId(long boardId) {
		jdbcTemplate.update(GRAPHIC_DESIGN_BOARD_ID_DELETE_SQL, new Object[] { boardId });
	}
	
	private static final String GRAPHIC_DESIGN_GRAPHIC_ID_DELETE_SQL = "delete from marmot_graphic_design where graphic_id =?";
	
	/**
	 * 根据图表ID删除图表设计
	 * @param graphicId 图表ID
	 */
	public void deleteGraphicDesignByGraphicId(long graphicId) {
		jdbcTemplate.update(GRAPHIC_DESIGN_GRAPHIC_ID_DELETE_SQL, new Object[] { graphicId });
	}
	
	private static final String GRAPHIC_DESIGN_FIND_NAME_SQL = "select graphic_id, graphic_name,graphic_code,board_id, graphic_type, graphic from marmot_graphic_design where graphic_code =?";
	
	/**
	 * 查询图表设计
	 * @param graphicCode 图表编码
	 * @return
	 */
	public GraphicDesign findGraphicDesign(String graphicCode) {
		return DataAccessUtils.singleResult(jdbcTemplate.query(GRAPHIC_DESIGN_FIND_NAME_SQL, new Object[] { graphicCode }, new RowMapper<GraphicDesign>() {
			
			public GraphicDesign mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDesign(rs);
			}
		}));
	}
	
	private static final String GRAPHIC_DESIGN_FIND_ID_SQL = "select graphic_id, graphic_name,board_id, graphic_type, graphic from marmot_graphic_design where graphic_id =?";
	
	/**
	 * 查询图表设计
	 * @param graphicId 图表Id
	 * @return
	 */
	public GraphicDesign findGraphicDesign(long graphicId) {
		return DataAccessUtils.singleResult(jdbcTemplate.query(GRAPHIC_DESIGN_FIND_ID_SQL, new Object[] { graphicId }, new RowMapper<GraphicDesign>() {
			
			public GraphicDesign mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDesign(rs);
			}
		}));
	}
	
	private static final String GRAPHIC_DESIGN_QUERY_SQL = "select graphic_id, graphic_name,board_id, graphic_type, graphic from marmot_graphic_design where board_id =?";
	
	/**
	 * 查询图表设计
	 * @param boardId 仪表盘ID
	 * @return
	 */
	public List<GraphicDesign> queryGraphicDesign(long boardId) {
		return jdbcTemplate.query(GRAPHIC_DESIGN_QUERY_SQL, new Object[] { boardId }, new RowMapper<GraphicDesign>() {
			
			public GraphicDesign mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDesign(rs);
			}
		});
	}
	
	private GraphicDesign buildGraphicDesign(ResultSet rs) throws SQLException {
		GraphicDesign graphicDesign = new GraphicDesign();
		graphicDesign.setGraphicId(rs.getLong(1));
		graphicDesign.setGraphicName(rs.getString(2));
		graphicDesign.setGraphicCode(rs.getString(3));
		graphicDesign.setBoardId(rs.getLong(4));
		GraphicType graphicType = GraphicType.getByCode(rs.getString(5));
		graphicDesign.setGraphicType(graphicType);
		GraphicConverter graphicConverter = ConverterAdapter.getInstance().getGraphicConverter(graphicType);
		graphicDesign.setGraphic(graphicConverter.parseGraphic(rs.getString(6)));
		return graphicDesign;
	}
	
	private static final String GRAPHIC_DOWNLOAD_STORE_SQL = "INSERT INTO marmot_graphic_download(founder_id, file_name,volume_code, graphic_code,graphic_type, graphic, file_url, download_url, status, memo) values (?,?,?,?,?,?,?,?,?,?)";
	
	/**
	 * 保持图表导出任务
	 * @param graphicDownload
	 */
	public void storeGraphicDownload(GraphicDownload graphicDownload) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(GRAPHIC_DOWNLOAD_STORE_SQL, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, graphicDownload.getFounderId());
				ps.setString(2, graphicDownload.getFileName());
				ps.setString(3, graphicDownload.getVolumeCode());
				ps.setString(4, graphicDownload.getGraphicCode());
				ps.setString(5, graphicDownload.getGraphicType().getCode());
				ps.setString(6, JSONObject.toJSONString(graphicDownload.getGraphic()));
				ps.setString(7, graphicDownload.getFileUrl());
				ps.setString(8, graphicDownload.getDownloadUrl());
				ps.setString(9, graphicDownload.getStatus().getCode());
				ps.setString(10, graphicDownload.getMemo());
				return ps;
			}
		}, keyHolder);
		graphicDownload.setDownloadId(keyHolder.getKey().longValue());
	}
	
	private static final String GRAPHIC_DOWNLOAD_UPDATE_SQL = "UPDATE marmot_graphic_download SET status=?,memo=? where download_id=?";
	
	/**
	 * 更新图表下载任务
	 * @param graphicDownload
	 */
	public void updateGraphicDownload(GraphicDownload graphicDownload) {
		jdbcTemplate.update(GRAPHIC_DOWNLOAD_UPDATE_SQL, new PreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, graphicDownload.getStatus().getCode());
				ps.setString(2, graphicDownload.getMemo());
				ps.setLong(3, graphicDownload.getDownloadId());
			}
		});
	}
	
	private static final String GRAPHIC_DOWNLOAD_DELETE_SQL = "delete from  marmot_graphic_download where download_id=?";
	
	/**
	 * 删除图表下载任务
	 * @param downloadId
	 */
	public void deleteGraphicDownload(long downloadId) {
		jdbcTemplate.update(GRAPHIC_DOWNLOAD_DELETE_SQL, new Object[] { downloadId });
	}
	
	private static final String GRAPHIC_DOWNLOAD_FIND_SQL = "select download_id, founder_id, file_name,volume_code,graphic_code, graphic_type, graphic, file_url, download_url, status, memo from marmot_graphic_download where download_id = ?";
	
	/**
	 * 根据下载ID获取图表下载任务
	 * @param downloadId
	 * @return
	 */
	public GraphicDownload findGraphicDownload(long downloadId) {
		return DataAccessUtils.singleResult(jdbcTemplate.query(GRAPHIC_DOWNLOAD_FIND_SQL, new Object[] { downloadId }, new RowMapper<GraphicDownload>() {
			
			public GraphicDownload mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDownload(rs);
			}
		}));
	}
	
	private static final String GRAPHIC_DOWNLOAD_LOAD_SQL = "select download_id, founder_id, file_name,volume_code, graphic_code,graphic_type, graphic, file_url, download_url, status, memo from marmot_graphic_download where download_id = ? for update ";
	
	/**
	 * 根据下载ID获取图表下载任务
	 * @param downloadId
	 * @return
	 */
	public GraphicDownload loadGraphicDownload(long downloadId) {
		return DataAccessUtils.singleResult(jdbcTemplate.query(GRAPHIC_DOWNLOAD_LOAD_SQL, new Object[] { downloadId }, new RowMapper<GraphicDownload>() {
			
			public GraphicDownload mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDownload(rs);
			}
		}));
	}
	
	/**
	 * 查询图表导出列表
	 * @param founderId
	 * @param graphicType
	 * @param fileName
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<GraphicDownload> queryPageGraphicDownloads(String founderId, String fileName, GraphicType graphicType, DownloadStatus status, OrderType orderType, int pageNum, int pageSize) {
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType,
			"SELECT download_id,founder_id, file_name, volume_code, graphic_code,graphic_type, graphic, file_url, download_url, status, memo FROM marmot_graphic_download");
		sqlBuilder.addCondition(Operators.equals, ColumnType.number, "founder_id", founderId).addCondition(Operators.like, ColumnType.string, "file_name", fileName)
			.addCondition(Operators.equals, ColumnType.string, "graphic_type", graphicType == null ? null : graphicType.getCode())
			.addCondition(Operators.equals, ColumnType.string, "status", status == null ? null : status.getCode()).addOrderBy("download_id", orderType).addLimit(pageNum, pageSize);
		return jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<GraphicDownload>() {
			
			public GraphicDownload mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildGraphicDownload(rs);
			}
		});
	}
	
	private GraphicDownload buildGraphicDownload(ResultSet rs) throws SQLException {
		GraphicDownload graphicDownload = new GraphicDownload();
		graphicDownload.setDownloadId(rs.getLong(1));
		graphicDownload.setFounderId(rs.getString(2));
		graphicDownload.setFileName(rs.getString(3));
		graphicDownload.setVolumeCode(rs.getString(4));
		graphicDownload.setGraphicCode(rs.getString(5));
		GraphicType graphicType = GraphicType.getByCode(rs.getString(6));
		graphicDownload.setGraphicType(graphicType);
		GraphicConverter graphicConverter = ConverterAdapter.getInstance().getGraphicConverter(graphicType);
		graphicDownload.setGraphic(graphicConverter.parseGraphic(rs.getString(7)));
		graphicDownload.setFileUrl(rs.getString(8));
		graphicDownload.setDownloadUrl(rs.getString(9));
		graphicDownload.setStatus(DownloadStatus.getByCode(rs.getString(10)));
		graphicDownload.setMemo(rs.getString(11));
		return graphicDownload;
	}
}
