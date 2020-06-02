package db.marmot.statistical;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.WindowType;
import db.marmot.enums.WindowUnit;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.convert.WindowUnitConverter;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	@NotBlank
	@Size(max = 512)
	private String volumeCode;
	
	/**
	 * 统计模型名
	 */
	@NotNull
	private String modelName;
	
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
	@NotNull
	@Size(min = 1)
	private List<AggregateColumn> aggregateColumns = new ArrayList<>();
	
	/**
	 * 统计条件
	 */
	@NotNull
	private List<ConditionColumn> conditionColumns = new ArrayList<>();
	
	/**
	 * 统计分组
	 */
	@NotNull
	private List<GroupColumn> groupColumns = new ArrayList<>();
	
	/**
	 * 统计方向
	 */
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
	
	public void validateStatisticalModel(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		groupColumns.forEach(groupColumn -> groupColumn.validateGroupColumn(dataVolume));
		aggregateColumns.forEach(aggregateColumn -> aggregateColumn.validateAggregateColumn(dataVolume));
		conditionColumns.forEach(conditionColumn -> conditionColumn.validateConditionColumn(dataVolume));
		directionColumns.forEach(directionColumn -> directionColumn.validateDirectionColumn(dataVolume));
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
		
		if (this.windowUnit != WindowUnit.non && timeValue != null) {
			ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
			WindowUnitConverter windowUnitConverter = converterAdapter.getWindowUnitConverter(this.windowUnit);
			
			long ts = converterAdapter.getGMT8Timestamp(timeValue).getTime();
			if (this.windowType == WindowType.simple_time) {
				long granularity = this.windowLength * windowUnitConverter.getTimeMillis();
				rowKeyBuilder.append("_").append(Long.MAX_VALUE - (ts - offset + (3600 * 1000 * 8)) / (granularity));
			}
			if (this.windowType == WindowType.sliding_time) {
				rowKeyBuilder.append("_").append(Long.MAX_VALUE - (ts - offset + (3600 * 1000 * 8)) / (windowUnitConverter.getTimeMillis()));
			}
		}
		
		return DigestUtils.md5Hex(rowKeyBuilder.toString());
	}
	
	public boolean revise(LocalDateTime localDateTime, int reviseDelay) {
		LocalDateTime modelLocalDateTime = rawUpdateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		modelLocalDateTime.plusMinutes(reviseDelay);
		return modelLocalDateTime.isBefore(localDateTime);
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
