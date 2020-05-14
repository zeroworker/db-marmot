package db.marmot.converter;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.util.JdbcUtils;
import db.marmot.enums.*;
import db.marmot.graphic.converter.DateCycleConverter;

/**
 * @author shaokang
 */
public class MySqlSelectSqlBuilderConverter implements SelectSqlBuilderConverter {
	
	private String tableAlias = "table_1";
	private SQLSelect sqlSelect = new SQLSelect();
	private MySqlSelectQueryBlock queryBlock = new MySqlSelectQueryBlock();
	private Map<Operators, OperatorsConverter> operatorsConverters;
	private Map<Aggregates, AggregatesConverter> aggregatesConverters;
	private Map<DateCycle, DateCycleConverter> dateCycleConverters;
	private SQLSelectGroupByClause sqlSelectGroupByClause;
	private SQLOrderBy sqlOrderBy;
	
	public MySqlSelectSqlBuilderConverter() {
	}
	
	public MySqlSelectSqlBuilderConverter(String sqlScript) {
		SQLExprParser parser = SQLParserUtils.createExprParser(sqlScript, JdbcUtils.MYSQL);
		if (parser.getLexer().token() != Token.SELECT) {
			throw new ConverterException("sql 必须为select sql");
		}
		queryBlock.setFrom(new SQLExprTableSource(new SQLIdentifierExpr("(" + sqlScript + ")"), tableAlias));
	}


	@Override
	public void setOperatorsConverters(Map<Operators, OperatorsConverter> operatorsConverters) {
		this.operatorsConverters = operatorsConverters;
	}

	@Override
	public void setAggregatesConverters(Map<Aggregates, AggregatesConverter> aggregatesConverters) {
		this.aggregatesConverters = aggregatesConverters;
	}

	@Override
	public void setDateCycleConverters(Map<DateCycle, DateCycleConverter> dateCycleConverters) {
		this.dateCycleConverters = dateCycleConverters;
	}

	@Override
	public SelectSqlBuilderConverter addSelectTable(String table) {
		queryBlock.setFrom(new SQLExprTableSource(new SQLIdentifierExpr(table)));
		return this;
	}

	@Override
	public SelectSqlBuilderConverter addSelectTable(String table, String alias) {
		queryBlock.setFrom(new SQLExprTableSource(new SQLIdentifierExpr(table),alias));
		return this;
	}

	@Override
	public MySqlSelectSqlBuilderConverter addCondition(Operators operators, ColumnType columnType, String columnCode, Object value) {
		if (!ObjectUtils.isEmpty(value)) {
			for (OperatorsConverter operatorsConverter : operatorsConverters.values()) {
				if (operatorsConverter.operators().equals(operators)) {
					operatorsConverter.validateValue(columnType, value);
					operatorsConverter.addCondition(this.queryBlock, columnType, columnCode, value);
					return this;
				}
			}
			throw new ConverterException(String.format("不支持的运算符·:%s", operators.getMessage()));
		}
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addNumberCondition(Operators operators, String columnCode, double unitValue, Object value) {
		if (unitValue > 0) {
			return addCondition(operators, ColumnType.number, columnCode + " * " + unitValue, value);
		}
		return addCondition(operators, ColumnType.number, columnCode, value);
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectItem(String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr(columnCode));
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectExprItem(String expr, String columnCode) {
		queryBlock.addSelectItem(new SQLIdentifierExpr(expr), columnCode);
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectDateCycleItem(String columnCode, DateCycle dateCycle) {
		if (dateCycle != DateCycle.non) {
			for (DateCycleConverter dateCycleConverter : dateCycleConverters.values()) {
				if (dateCycleConverter.dateCycle().equals(dateCycle)) {
					dateCycleConverter.addSelectItem(this.queryBlock, columnCode);
					return this;
				}
			}
			throw new ConverterException(String.format("不支持的时间周期·:%s", dateCycle.getMessage()));
		}
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectNumberItem(String columnCode, double unitValue) {
		if (unitValue > 0) {
			queryBlock.addSelectItem(new SQLIdentifierExpr(columnCode + " * " + unitValue), columnCode);
			return this;
		}
		queryBlock.addSelectItem(new SQLIdentifierExpr(columnCode));
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectAggregateItem(Aggregates aggregates, String columnCode, double unitValue) {
		if (unitValue > 0) {
			return addSelectAggregateItem(aggregates, columnCode + " * " + unitValue, columnCode);
		}
		return addSelectAggregateItem(aggregates, null, columnCode);
	}
	
	@Override
	public SelectSqlBuilderConverter addSelectAggregateItem(Aggregates aggregates, String expr, String columnCode) {
		if (aggregates != Aggregates.non && aggregates != Aggregates.calculate) {
			for (AggregatesConverter aggregatesConverter : aggregatesConverters.values()) {
				if (aggregatesConverter.aggregates().equals(aggregates)) {
					if (StringUtils.isNotBlank(expr)) {
						aggregatesConverter.addSelectItem(this.queryBlock, expr, columnCode);
						return this;
					}
					aggregatesConverter.addSelectItem(this.queryBlock, columnCode);
					return this;
				}
			}
		}
		throw new ConverterException(String.format("不支持的聚合函数·:%s", aggregates.getMessage()));
	}
	
	@Override
	public SelectSqlBuilderConverter addOrderBy(String columnCode, OrderType orderType) {
		if (OrderType.non != orderType) {
			if (sqlOrderBy == null) {
				sqlOrderBy = new SQLOrderBy();
			}
			sqlOrderBy.addItem(new SQLSelectOrderByItem(new SQLIdentifierExpr(columnCode + " " + orderType.getCode())));
		}
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addGroupBy(String columnCode) {
		if (sqlSelectGroupByClause == null) {
			sqlSelectGroupByClause = new SQLSelectGroupByClause();
		}
		sqlSelectGroupByClause.addItem(new SQLIdentifierExpr(columnCode));
		return this;
	}
	
	@Override
	public SelectSqlBuilderConverter addDateCycleGroupBy(String columnCode, DateCycle dateCycle) {
		if (dateCycle != DateCycle.non) {
			for (DateCycleConverter dateCycleConverter : dateCycleConverters.values()) {
				if (dateCycleConverter.dateCycle().equals(dateCycle)) {
					dateCycleConverter.addGroupBy(this.sqlSelectGroupByClause, columnCode);
					return this;
				}
			}
			throw new ConverterException(String.format("不支持的时间周期·:%s", dateCycle.getMessage()));
		}
		return this;
	}
	
	@Override
	public MySqlSelectSqlBuilderConverter addLimit(int pageNum, int pageSize) {
		queryBlock.setLimit(new SQLLimit(new SQLNumberExpr(pageNum > 0 ? (pageNum - 1) * pageSize : 0), new SQLNumberExpr(pageSize)));
		return this;
	}
	
	@Override
	public String toSql() {
		List<SQLSelectItem> sqlSelectItems = queryBlock.getSelectList();
		if (sqlSelectItems == null || sqlSelectItems.size() == 0) {
			queryBlock.addSelectItem(new SQLIdentifierExpr("*"));
		}
		if (sqlSelectGroupByClause != null) {
			queryBlock.setGroupBy(sqlSelectGroupByClause);
		}
		if (sqlOrderBy != null) {
			queryBlock.setOrderBy(sqlOrderBy);
		}
		sqlSelect.setQuery(queryBlock);
		return SQLUtils.toSQLString(sqlSelect);
	}
}
