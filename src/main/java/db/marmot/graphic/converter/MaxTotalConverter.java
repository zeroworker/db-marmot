package db.marmot.graphic.converter;

import java.math.BigDecimal;
import java.util.List;

import db.marmot.enums.TotalType;

/**
 * @author shaokang
 */
public class MaxTotalConverter implements TotalConverter {
	@Override
	public TotalType totalType() {
		return TotalType.max;
	}
	
	@Override
	public BigDecimal calculateTotalValue(List<BigDecimal> values) {
		BigDecimal totalValue = null;
		for (BigDecimal decimal : values) {
			if (totalValue == null) {
				totalValue = decimal;
				continue;
			}
			
			totalValue = totalValue.max(decimal);
		}
		return totalValue;
	}
}
