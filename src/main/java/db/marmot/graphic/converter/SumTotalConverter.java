package db.marmot.graphic.converter;

import java.math.BigDecimal;
import java.util.List;

import db.marmot.enums.TotalType;

/**
 * @author shaokang
 */
public class SumTotalConverter implements TotalConverter {
	@Override
	public TotalType totalType() {
		return TotalType.sum;
	}
	
	@Override
	public BigDecimal calculateTotalValue(List<BigDecimal> values) {
		BigDecimal totalValue = BigDecimal.ZERO;
		for (BigDecimal decimal : values) {
			totalValue = totalValue.add(decimal);
		}
		return totalValue;
	}
}
