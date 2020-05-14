package db.marmot.statistical;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalDistinct {
	
	/**
	 * 序列ID
	 */
	private long distinctId;
	
	/**
	 * 模型名称
	 */
	private String modelName;
	
	/**
	 * 统计数据唯一标识
	 */
	private String rowKey;
	
	/**
	 * 去重字段
	 */
	private String distinctColumn;
	
	/**
	 * 去重数据
	 */
	private Set<Object> distinctData;
	
	/**
	 * 更新时间
	 */
	private Date rawUpdateTime;

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StatisticalDistinct that = (StatisticalDistinct) o;
		return Objects.equals(rowKey, that.rowKey) && Objects.equals(distinctColumn, that.distinctColumn);
	}

	public StatisticalDistinct() {
	}

	public StatisticalDistinct(String rowKey, String distinctColumn) {
		this.rowKey = rowKey;
		this.distinctColumn = distinctColumn;
	}

	public boolean addDistinctData(Object distinctValue) {
		if (this.distinctData == null) {
			this.distinctData = new HashSet<>();
		}
		return this.distinctData.add(distinctValue);
	}
	
	public String uniqueKey() {
		return StringUtils.join(rowKey, "_", distinctColumn);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rowKey, distinctColumn);
	}
}
