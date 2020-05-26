package db.marmot.statistical;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.WindowType;
import db.marmot.enums.WindowUnit;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.convert.WindowUnitConverter;
import db.marmot.volume.Database;
import db.marmot.volume.parser.SelectTable;
import db.marmot.volume.parser.SqlSelectQueryParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalModel {
	
	/**
	 * 序列ID
	 */
	private long modelId;
	
	/**
	 * 数据编码
	 */
	private String volumeCode;
	
	/**
	 * 统计模型名
	 */
	@NotNull
	private String modelName;
	
	/**
	 * 数据源名
	 */
	@NotNull
	private String dbName;
	
	/**
	 * 抓取数据sql
	 */
	@NotNull
	private String fetchSql;
	
	/**
	 * 抓取数据步长
	 */
	private long fetchStep;
	
	/**
	 * 模型是否运行中
	 */
	private boolean running = true;
	
	/**
	 * 是否已完成计算
	 */
	private boolean calculated = true;
	
	/**
	 * 偏移量表达式
	 */
	@NotNull
	private List<String> offsetExpr = new ArrayList<>();
	
	/**
	 * 角标字段
	 */
	@NotNull
	private String indexColumn;
	
	/**
	 * 时间字段
	 */
	@NotNull
	private String timeColumn;
	
	/**
	 * 统计窗口长度
	 */
	private int windowLength;
	
	/**
	 * 窗口类型
	 */
	@NotNull
	private WindowType windowType;
	
	/**
	 * 窗口粒度
	 */
	@NotNull
	private WindowUnit windowUnit;
	
	/**
	 * 统计聚合字段
	 */
	@Valid
	@NotNull
	@Size(min = 1)
	private List<AggregateColumn> aggregateColumns = new ArrayList<>();
	
	/**
	 * 统计条件
	 */
	@Valid
	@NotNull
	private List<ConditionColumn> conditionColumns = new ArrayList<>();
	
	/**
	 * 统计分组
	 */
	@Valid
	@NotNull
	private List<GroupColumn> groupColumns = new ArrayList<>();
	
	/**
	 * 统计方向
	 */
	@Valid
	@NotNull
	private List<DirectionColumn> directionColumns = new ArrayList<>();
	
	/**
	 * 模型说明
	 */
	private String memo;
	
	/**
	 * 更新时间
	 */
	private Date rawUpdateTime;
	
	public void validateStatisticalModel(Database database) {
		Validators.assertJSR303(this);
		SqlSelectQueryParser sqlSelectQueryParser = new SqlSelectQueryParser(database.getDbType(), fetchSql).parse();
		List<SelectTable> selectTables = sqlSelectQueryParser.getSelectTables();
		if (selectTables.size() != 1) {
			throw new ValidateException("模型fetch sql必须为单表查询");
		}
		if (sqlSelectQueryParser.getSelectColumn("id") == null) {
			throw new ValidateException("模型fetch sql 查询字段必须包含自增ID(id)");
		}
		if (sqlSelectQueryParser.getSelectColumn(timeColumn) == null) {
			throw new ValidateException("模型fetch sql 查询字段必须包含 timeColumn");
		}
		for (AggregateColumn column : aggregateColumns) {
			if (sqlSelectQueryParser.getSelectColumn(column.getColumnCode()) == null) {
				throw new ValidateException(String.format("模型聚合字段%s fetch sql中未定义", column.getColumnCode()));
			}
		}
		for (ConditionColumn column : conditionColumns) {
			if (sqlSelectQueryParser.getSelectColumn(column.getColumnCode()) == null) {
				throw new ValidateException(String.format("模型条件字段%s fetch sql中未定义", column.getColumnCode()));
			}
		}
		for (GroupColumn column : groupColumns) {
			if (sqlSelectQueryParser.getSelectColumn(column.getColumnCode()) == null) {
				throw new ValidateException(String.format("模型分组字段%s fetch sql中未定义", column.getColumnCode()));
			}
		}
		for (DirectionColumn column : directionColumns) {
			if (sqlSelectQueryParser.getSelectColumn(column.getColumnCode()) == null) {
				throw new ValidateException(String.format("模型方向字段%s fetch sql中未定义", column.getColumnCode()));
			}
		}
		
		for (String expr : offsetExpr) {
			try {
				Integer.valueOf(ConverterAdapter.getInstance().eval(expr).toString());
			} catch (Exception e) {
				throw new ValidateException(String.format("无法解析偏移量表达式:%s", expr));
			}
		}
	}
	
	public void addOffsetExpr(String expr) {
		this.offsetExpr.add(expr);
	}
	
	public String createRowKey(Map<String, Object> groupData, Date timeValue, int offset) {
		StringBuilder rowKeyBuilder = new StringBuilder(this.modelName);
		if (groupData != null && !groupData.isEmpty()) {
			for (GroupColumn column : groupColumns) {
				Object groupValue = groupData.get(column.getColumnCode());
				if (groupValue != null) {
					rowKeyBuilder.append("_").append(groupValue);
				}
			}
		}
		
		if (this.windowUnit != WindowUnit.NON && timeValue != null) {
			ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
			WindowUnitConverter windowUnitConverter = converterAdapter.getWindowUnitConverter(this.windowUnit);
			
			long ts = converterAdapter.getGMT8Timestamp(timeValue).getTime();
			if (this.windowType == WindowType.SIMPLE_TIME) {
				long granularity = this.windowLength * windowUnitConverter.getTimeMillis();
				rowKeyBuilder.append("_").append(Long.MAX_VALUE - (ts - offset + (3600 * 1000 * 8)) / (granularity));
			}
			if (this.windowType == WindowType.SLIDING_TIME) {
				rowKeyBuilder.append("_").append(Long.MAX_VALUE - (ts - offset + (3600 * 1000 * 8)) / (windowUnitConverter.getTimeMillis()));
			}
		}
		
		return DigestUtils.md5Hex(rowKeyBuilder.toString());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StatisticalModel that = (StatisticalModel) o;
		return Objects.equals(modelName, that.modelName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(modelName);
	}
}
