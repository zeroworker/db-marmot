package db.marmot.graphic.converter;

import java.util.Date;

import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.DateCycle;

/**
 * @author shaokang
 */
public interface DateCycleConverter {
	
	/**
	 * 时间周期
	 * @return
	 */
	DateCycle dateCycle();
	
	/**
	 * 添加查询字段
	 * @param queryBlock
	 * @param columnCode
	 */
	void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode);
	
	/**
	 * 添加分组字段
	 * @param sqlSelectGroupByClause
	 * @param columnCode
	 */
	void addGroupBy(SQLSelectGroupByClause sqlSelectGroupByClause, String columnCode);
	
	/**
	 * 转换时间值
	 * @param date
	 * @param format
	 * @return
	 */
	String convertValue(Date date, String format);
}
