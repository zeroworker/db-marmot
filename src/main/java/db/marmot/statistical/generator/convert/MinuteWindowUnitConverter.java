package db.marmot.statistical.generator.convert;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import db.marmot.enums.WindowUnit;

/**
 * @author shaokang
 */
public class MinuteWindowUnitConverter implements WindowUnitConverter {
	
	@Override
	public WindowUnit windowUnit() {
		return WindowUnit.MINUTE;
	}
	
	@Override
	public long getTimeMillis() {
		return 60L * 1000;
	}
	
	@Override
	public Date getTimeUnit(Date date, int offset) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
		localDate.atTime(zonedDateTime.getHour(), zonedDateTime.getMinute(), 0, offset);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
