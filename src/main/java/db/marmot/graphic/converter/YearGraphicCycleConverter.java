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
public class YearGraphicCycleConverter implements GraphicCycleConverter {
	
	@Override
	public GraphicCycle graphicCycle() {
		return GraphicCycle.year;
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("date_format(" + columnCode + ", \"%Y\")"), columnCode);
	}
	
	@Override
	public void addGroupBy(SQLSelectGroupByClause sqlSelectGroupByClause, String columnCode) {
		sqlSelectGroupByClause.addItem(new SQLIdentifierExpr("date_format(" + columnCode + ", \"%Y\")"));
	}
	
	@Override
	public String convertValue(Date date, String format) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
		return dateTimeFormatter.format(localDate);
	}
}
