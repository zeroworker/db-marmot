package db.marmot.statistical;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalTask {
	
	/**
	 * 序列ID
	 */
	private long taskId;
	
	/**
	 * 模型名称
	 */
	private String modelName;
	
	/**
	 * 是否已扫描
	 */
	private boolean scanned;
	
	/**
	 * 开始角标
	 */
	private long startIndex;
	
	/**
	 * 结束角标
	 */
	private long endIndex;
	
	/**
	 * 更新时间
	 */
	private Date rawUpdateTime;
}
