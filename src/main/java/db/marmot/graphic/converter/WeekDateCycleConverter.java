package db.marmot.graphic.converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.DateCycle;

/**
 * @author shaokang
 */
public class WeekDateCycleConverter implements DateCycleConverter {
	
	@Override
	public DateCycle dateCycle() {
		return DateCycle.week;
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("CONCAT(date_format(" + columnCode + ", \"%x-%v\"),'周')"), columnCode);
	}
	
	@Override
	public void addGroupBy(SQLSelectGroupByClause sqlSelectGroupByClause, String columnCode) {
		sqlSelectGroupByClause.addItem(new SQLIdentifierExpr("date_format(" + columnCode + ", \"%x-%v\")"));
	}
	
	@Override
	public String convertValue(Date date, String format) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-w周");
		return dateTimeFormatter.format(localDate);
	}
}
