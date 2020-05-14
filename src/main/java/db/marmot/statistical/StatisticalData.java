package db.marmot.statistical;

import java.util.*;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalData {
	
	/**
	 * 序列ID
	 */
	private long dataId;
	
	/**
	 * 模型名称
	 */
	@NotBlank
	private String modelName;
	
	/**
	 * 统计数据唯一标识
	 */
	@NotBlank
	private String rowKey;
	
	/**
	 * 统计数据
	 */
	@NotBlank
	private Map<String, Object> aggregateData = new HashMap<>();
	
	/**
	 * 分组值
	 */
	@NotBlank
	private List<Object> groupColumns = new ArrayList<>();
	
	/**
	 * 统计时间粒度
	 */
	private Date timeUnit;
	
	/**
	 * 更新时间
	 */
	private Date rawUpdateTime;
	
	public StatisticalData() {
	}
	
	public StatisticalData(String modelName, String rowKey) {
		this.modelName = modelName;
		this.rowKey = rowKey;
	}
	
	public <T> T putIfPresent(String key, T value) {
		if (aggregateData.containsKey(key)) {
			return (T) aggregateData.get(key);
		}
		aggregateData.put(key, value);
		return value;
	}
	
	public <T> T getIfPresent(String key, T value) {
		if (aggregateData.containsKey(key)) {
			return (T) aggregateData.get(key);
		}
		return value;
	}
	
	public void addAggregateData(String key, Object value) {
		aggregateData.put(key, value);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StatisticalData that = (StatisticalData) o;
		return Objects.equals(rowKey, that.rowKey);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rowKey);
	}
}
