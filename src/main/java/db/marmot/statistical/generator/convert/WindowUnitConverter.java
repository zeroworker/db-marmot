package db.marmot.statistical.generator.convert;

import java.util.Date;

import db.marmot.enums.WindowUnit;

/**
 * @author shaokang
 */
public interface WindowUnitConverter {
	
	/**
	 * 窗口粒度
	 * @return
	 */
	WindowUnit windowUnit();
	
	/**
	 * 获取窗口粒度毫秒数
	 * @return
	 */
	long getTimeMillis();
	
	/**
	 * 获取窗口时间
	 * @param date
	 * @param offset
	 * @return
	 */
	Date getTimeUnit(Date date, int offset);
}
