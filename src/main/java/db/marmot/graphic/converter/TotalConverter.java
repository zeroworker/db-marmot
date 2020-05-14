package db.marmot.graphic.converter;

import java.math.BigDecimal;
import java.util.List;

import db.marmot.enums.TotalType;

/**
 * @author shaokang
 */
public interface TotalConverter {
	
	/**
	 * 合计方式
	 * @return
	 */
	TotalType totalType();
	
	/**
	 * 计算合计值
	 * @param values
	 * @return
	 */
	BigDecimal calculateTotalValue(List<BigDecimal> values);
}
