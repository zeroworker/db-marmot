package db.marmot.graphic.converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import db.marmot.enums.GraphicCycle;

/**
 * @author shaokang
 */
public class SeasonGraphicCycleConverter implements GraphicCycleConverter {
	
	@Override
	public GraphicCycle graphicCycle() {
		return GraphicCycle.season;
	}
	
	@Override
	public void addSelectItem(MySqlSelectQueryBlock queryBlock, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr("CONCAT(YEAR(" + columnCode + "),'-',quarter(" + columnCode + "),'季度')"), columnCode);
	}
	
	@Override
	public void addGroupBy(SQLSelectGroupByClause sqlSelectGroupByClause, String columnCode) {
		sqlSelectGroupByClause.addItem(new SQLIdentifierExpr("CONCAT(YEAR(" + columnCode + "),'-',quarter(" + columnCode + "))"));
	}
	
	@Override
	public String convertValue(Date date, String format) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-Q季度");
		return dateTimeFormatter.format(localDate);
	}
}
