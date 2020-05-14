package db.marmot.volume;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class DataRange {
	
	/**
	 * 最小值
	 */
	private long minValue;
	
	/**
	 * 最大值
	 */
	private long maxValue;
	
	/**
	 * 计算结束index
	 * @param step
	 * @return
	 */
	public long calculateEndIndex(long step) {
		long stepIndex = minValue + step;
		return maxValue <= stepIndex ? maxValue : stepIndex;
	}
}
