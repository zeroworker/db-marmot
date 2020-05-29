package db.marmot.statistical.generator.convert;

import db.marmot.enums.WindowUnit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author shaokang
 */
public class DayWindowUnitConverter implements WindowUnitConverter {
	
	@Override
	public WindowUnit windowUnit() {
		return WindowUnit.day;
	}
	
	@Override
	public long getTimeMillis() {
		return 24 * 60 * 60L * 1000;
	}
	
	@Override
	public Date getTimeUnit(Date date, int offset) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		localDate.atTime(0, 0, 0, offset);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
