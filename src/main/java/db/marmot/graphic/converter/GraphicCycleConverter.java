package db.marmot.graphic.converter;

import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.GraphicCycle;
import db.marmot.enums.WindowUnit;

import java.util.Date;

/**
 * @author shaokang
 */
public interface GraphicCycleConverter {
	
	/**
	 * 图表周期
	 * @return
	 */
	GraphicCycle graphicCycle();
	
	/**
	 * 窗口粒度
	 * @return
	 */
	WindowUnit windowUnit();
	
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
