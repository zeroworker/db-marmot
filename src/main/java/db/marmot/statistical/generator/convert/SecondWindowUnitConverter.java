package db.marmot.statistical.generator.convert;

import db.marmot.enums.WindowUnit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author shaokang
 */
public class SecondWindowUnitConverter implements WindowUnitConverter {
	
	@Override
	public WindowUnit windowUnit() {
		return WindowUnit.second;
	}
	
	@Override
	public long getTimeMillis() {
		return 1L * 1000;
	}
	
	@Override
	public Date getTimeUnit(Date date, int offset) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
		localDate.atTime(zonedDateTime.getHour(), zonedDateTime.getMinute(), zonedDateTime.getSecond(), offset);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
