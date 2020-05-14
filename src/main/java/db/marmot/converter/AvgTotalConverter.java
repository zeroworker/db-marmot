package db.marmot.converter;

import java.math.BigDecimal;
import java.util.List;

import db.marmot.enums.TotalType;
import db.marmot.graphic.converter.TotalConverter;

/**
 * @author shaokang
 */
public class AvgTotalConverter implements TotalConverter {
	@Override
	public TotalType totalType() {
		return TotalType.avg;
	}
	
	@Override
	public BigDecimal calculateTotalValue(List<BigDecimal> values) {
		BigDecimal totalValue = BigDecimal.ZERO;
		for (BigDecimal decimal : values) {
			totalValue = totalValue.add(decimal);
		}
		return totalValue.divide(new BigDecimal(values.size()));
	}
}
