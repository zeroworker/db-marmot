package db.marmot.graphic.converter;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.GraphicCycle;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author shaokang
 */
public class MinuteGraphicCycleConverter implements GraphicCycleConverter {
	
	@Override
	public GraphicCycle graphicCycle() {
		return GraphicCycle.minute;
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("date_format(" + columnCode + ", \"%Y-%m-%d %H:%i\")"), columnCode);
	}
	
	@Override
	public void addGroupBy(SQLSelectGroupByClause sqlSelectGroupByClause, String columnCode) {
		sqlSelectGroupByClause.addItem(new SQLIdentifierExpr("date_format(" + columnCode + ", \"%Y-%m-%d %H:%i\")"));
	}
	
	@Override
	public String convertValue(Date date, String format) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return dateTimeFormatter.format(localDate);
	}
}
