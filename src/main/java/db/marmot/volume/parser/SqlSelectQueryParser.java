package db.marmot.volume.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.Operators;
import db.marmot.repository.validate.Validators;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据集查询sql解析器，定义相应的数据集sql规则,不满足数据集sql规则无法解析
 * </p>
 * <p>
 * 1.sql 必须为查询sql ,查询字段不支持聚合函数,不支持运算字段
 * </p>
 * <p>
 * 1.sql 查询字段、表、以及条件必须带上 表别名
 * </p>
 * <p>
 * 2.sql from 只支持单表、联表查询,不支持子查询、视图等等
 * </p>
 * <p>
 * 3.sql where 允许为空 若字段为日期 格式定义为yyyy-MM-dd HH:mm:ss 解析条件右值为时间,非该格式默认为字符串
 * </p>
 * <p>
 * 4.sql 不允许存在 group by 或者 order by having 等
 * </p>
 * <p>
 * 5.sql 格式 select a.xx from table_1 a where a.id=1
 * </p>
 * @author shaokang
 */
public class SqlSelectQueryParser {
	
	private SQLSelectQueryBlock sqlSelectQueryBlock;
	private List<SelectColumn> selectColumns = new ArrayList<>();
	private List<SelectTable> selectTables = new ArrayList<>();
	private List<SelectCondition> selectConditions = new ArrayList<>();
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public SqlSelectQueryParser(String sqlType, String sql) {
		
		Validators.notNull(sql, "sql 不能为空");
		Validators.notNull(sqlType, "sqlType 不能为空");
		
		SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, sqlType);
		if (sqlStatementParser.getLexer().token() != Token.SELECT) {
			throw new SqlParserException("sql 必须为select sql");
		}
		
		SQLStatement sqlStatement = sqlStatementParser.parseStatementList().stream().findFirst().get();
		this.sqlSelectQueryBlock = (SQLSelectQueryBlock) ((SQLSelectStatement) sqlStatement).getSelect().getQuery();
		
		if (sqlSelectQueryBlock.getGroupBy() != null) {
			throw new SqlParserException("查询sql不能包含group by子句");
		}
		
		if (sqlSelectQueryBlock.getOrderBy() != null) {
			throw new SqlParserException("查询sql不能包含order by子句");
		}
		
		if (sqlSelectQueryBlock.getLimit() != null) {
			throw new SqlParserException("查询sql不能包含limit子句");
		}
		
		if (sqlSelectQueryBlock.isForUpdate() || sqlSelectQueryBlock.isNoWait()) {
			throw new SqlParserException("查询sql不能包含数据库锁表子句");
		}
	}
	
	/**
	 * 是否存在条件
	 * @return
	 */
	public boolean exitSelectConditions() {
		return sqlSelectQueryBlock.getWhere() != null;
	}
	
	/**
	 * 获取解析表
	 * @return
	 */
	public List<SelectTable> getSelectTables() {
		return selectTables;
	}
	
	/**
	 * 根据查询表获取表对应的查询字段
	 * @param selectTable
	 * @return
	 */
	public List<SelectColumn> getSelectColumns(SelectTable selectTable) {
		return selectColumns.stream().filter(selectColumn -> selectColumn.getTableAlias().endsWith(selectTable.getTableAlias())).collect(Collectors.toList());
	}
	
	/**
	 * 根据查询表获取表对应的查询字段
	 * @param selectTable
	 * @return
	 */
	public List<SelectCondition> getSelectConditions(SelectTable selectTable) {
		return selectConditions.stream().filter(selectColumn -> selectColumn.getTableAlias().endsWith(selectTable.getTableAlias())).collect(Collectors.toList());
	}
	
	/**
	 * 解析sql table -> column -> condition
	 * @return
	 */
	public SqlSelectQueryParser parse() {
		
		SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
		
		if (sqlTableSource instanceof SQLExprTableSource) {
			//-解析单表
			parseSelectTable(sqlTableSource);
			//-解析查询字段
			parseSelectColumns(sqlSelectQueryBlock.getSelectList());
			//-解析查询条件
			parseSelectConditions(sqlSelectQueryBlock.getWhere());
			return this;
		}
		
		if (sqlTableSource instanceof SQLJoinTableSource) {
			//-解析联表
			SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
			parseSelectTable(sqlJoinTableSource.getLeft());
			parseSelectTable(sqlJoinTableSource.getRight());
			//-解析查询字段
			parseSelectColumns(sqlSelectQueryBlock.getSelectList());
			//-解析查询条件
			parseSelectConditions(sqlSelectQueryBlock.getWhere());
			return this;
		}
		
		throw new SqlParserException("查询sql 只支持单表查询或者联表查询");
	}
	
	/**
	 * 解析查询表
	 * @param sqlTableSource
	 */
	private void parseSelectTable(SQLTableSource sqlTableSource) {
		if (!(sqlTableSource instanceof SQLExprTableSource)) {
			throw new SqlParserException(String.format("表%s必须为单表", sqlTableSource.toString()));
		}
		
		SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
		if (StringUtils.isBlank(sqlExprTableSource.getAlias())) {
			throw new SqlParserException(String.format("表%s必须存在别名", sqlExprTableSource.getExpr().toString()));
		}
		
		String tableName = sqlExprTableSource.getExpr().toString();
		selectTables.add(new SelectTable(tableName, sqlExprTableSource.getAlias()));
	}
	
	/**
	 * 解析查询字段
	 * @param sqlSelectItems
	 */
	private void parseSelectColumns(List<SQLSelectItem> sqlSelectItems) {
		if (sqlSelectItems == null || sqlSelectItems.size() == 0) {
			throw new SqlParserException("必须定义查询字段");
		}
		
		//-解析查询字段
		for (SQLSelectItem sqlSelectItem : sqlSelectItems) {
			SQLExpr sqlExpr = sqlSelectItem.getExpr();
			
			if (sqlExpr instanceof SQLAllColumnExpr) {
				throw new SqlParserException("必须指定查询字段,不允许使用*");
			}
			
			if (!(sqlExpr instanceof SQLPropertyExpr)) {
				throw new SqlParserException(String.format("查询字段%s必须指定表名,并且不支持计算表达式或者函数表达式", sqlSelectItem.getExpr().toString()));
			}
			
			SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
			
			//-验证字段表别名是否存在
			String tableAlias = sqlPropertyExpr.getOwner().toString();
			SelectTable selectTable = getSelectTable(tableAlias);
			if (selectTable == null) {
				throw new SqlParserException(String.format("查询字段%s必须指定表别名", sqlSelectItem.getExpr().toString()));
			}
			
			//-若查询字段未指定别名,默认使用字段名
			String columnAlias = sqlSelectItem.getAlias();
			if (StringUtils.isBlank(columnAlias)) {
				columnAlias = sqlPropertyExpr.getName();
			}
			
			if (!selectColumns.add(new SelectColumn(sqlPropertyExpr.getName(), columnAlias, tableAlias))) {
				throw new SqlParserException(String.format("存在相同的查询字段%s", sqlSelectItem.getAlias()));
			}
		}
	}
	
	/**
	 * 解析查询条件
	 * @param whereSqlExpr
	 */
	private void parseSelectConditions(SQLExpr whereSqlExpr) {
		//-允许无条件存在的sql
		if (whereSqlExpr != null) {
			//-in 条件处理
			if (whereSqlExpr instanceof SQLInListExpr) {
				parseSelectInCondition((SQLInListExpr) whereSqlExpr);
				return;
			}
			
			if (!(whereSqlExpr instanceof SQLBinaryOpExpr)) {
				throw new SqlParserException(String.format("条件只支持单字段比较", whereSqlExpr.toString()));
			}
			
			//-非in 条件处理
			if (whereSqlExpr instanceof SQLBinaryOpExpr) {
				SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) whereSqlExpr;
				if (sqlBinaryOpExpr.getLeft() instanceof SQLBinaryOpExpr) {
					//- 多条件之间只允许存在and条件
					if (sqlBinaryOpExpr.getOperator() != SQLBinaryOperator.BooleanAnd) {
						throw new SqlParserException(String.format("多条件只支持 and ", whereSqlExpr.toString()));
					}
					//-递归解析左值和右值
					parseSelectConditions(sqlBinaryOpExpr.getLeft());
					parseSelectConditions(sqlBinaryOpExpr.getRight());
					return;
				}
				//-左值为in
				if (sqlBinaryOpExpr.getLeft() instanceof SQLInListExpr) {
					if (sqlBinaryOpExpr.getOperator() != SQLBinaryOperator.BooleanAnd) {
						throw new SqlParserException(String.format("多条件只支持 and ", whereSqlExpr.toString()));
					}
					parseSelectInCondition((SQLInListExpr) sqlBinaryOpExpr.getLeft());
					parseSelectConditions(sqlBinaryOpExpr.getRight());
					return;
				}
				//解析条件字段
				parseSelectOtherCondition(sqlBinaryOpExpr);
			}
		}
	}
	
	/**
	 * 解析其他比较符条件
	 * @param sqlBinaryOpExpr
	 */
	private void parseSelectOtherCondition(SQLBinaryOpExpr sqlBinaryOpExpr) {
		
		SelectCondition selectCondition = createSelectCondition(sqlBinaryOpExpr.getLeft());
		//-设置比较符
		selectCondition.setOperators(converterAdapter.getOperatorsConverter(sqlBinaryOpExpr.getOperator()).operators());
		
		//-设置条件右值
		//-文本类型
		if (sqlBinaryOpExpr.getRight() instanceof SQLTextLiteralExpr) {
			SQLTextLiteralExpr sqlTextLiteralExpr = (SQLTextLiteralExpr) sqlBinaryOpExpr.getRight();
			try {
				LocalDate localDate = LocalDate.parse(sqlTextLiteralExpr.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				selectCondition.setRightValue(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} catch (Exception e) {
				selectCondition.setRightValue(sqlTextLiteralExpr.getText());
			}
		}
		//-数字类型
		if (sqlBinaryOpExpr.getRight() instanceof SQLNumericLiteralExpr) {
			SQLNumericLiteralExpr sqlNumericLiteralExpr = (SQLNumericLiteralExpr) sqlBinaryOpExpr.getRight();
			selectCondition.setRightValue(new BigDecimal(sqlNumericLiteralExpr.getNumber().toString()));
		}
		if (selectCondition.getRightValue() == null) {
			throw new SqlParserException(String.format("条件字段%s右值为空", selectCondition.getColumnCode()));
		}
		
		selectConditions.add(selectCondition);
	}
	
	/**
	 * 解析比较符in 的条件
	 * @param sqlInListExpr
	 */
	private void parseSelectInCondition(SQLInListExpr sqlInListExpr) {
		
		SelectCondition selectCondition = createSelectCondition(sqlInListExpr.getExpr());
		//-比较符指定为in
		selectCondition.setOperators(sqlInListExpr.isNot() ? Operators.not_in : Operators.in);
		
		//-解析比较右值 只允许存在字符串
		List<String> rightValue = new ArrayList();
		for (SQLExpr sqlExpr : sqlInListExpr.getTargetList()) {
			if (sqlExpr instanceof SQLNumericLiteralExpr) {
				throw new SqlParserException(String.format("条件字段%s比较符为in只支持字符串", selectCondition.getColumnCode()));
			}
			if (sqlExpr instanceof SQLTextLiteralExpr) {
				SQLTextLiteralExpr sqlTextLiteralExpr = (SQLTextLiteralExpr) sqlExpr;
				rightValue.add(sqlTextLiteralExpr.getText());
				continue;
			}
			throw new SqlParserException(String.format("无法识别条件字段%s右值类型", selectCondition.getColumnCode()));
		}
		if (rightValue.isEmpty()) {
			throw new SqlParserException(String.format("条件字段%s右值为空", selectCondition.getColumnCode()));
		}
		selectCondition.setRightValue(rightValue);
		
		selectConditions.add(selectCondition);
	}
	
	private SelectCondition createSelectCondition(SQLExpr conditionSqlExpr) {
		SelectCondition selectCondition = new SelectCondition();
		//-解析条件字段
		//-未指定表别名为别名字段,必须在查询字段中存在
		if (conditionSqlExpr instanceof SQLIdentifierExpr) {
			SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) conditionSqlExpr;
			SelectColumn selectColumn = getSelectColumn(sqlIdentifierExpr.getName());
			if (selectColumn == null) {
				throw new SqlParserException(String.format("查询字段中必须存在别名条件字段%s", sqlIdentifierExpr.getName()));
			}
			selectCondition.setColumnCode(selectColumn.getColumnCode());
			selectCondition.setColumnCode(selectColumn.getColumnAlias());
			selectCondition.setTableAlias(selectColumn.getTableAlias());
		}
		//-指定表别名条件字段 该字段为表原始字段
		if (conditionSqlExpr instanceof SQLPropertyExpr) {
			SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) conditionSqlExpr;
			selectCondition.setColumnCode(sqlPropertyExpr.getName());
			SelectTable selectTables = getSelectTable(sqlPropertyExpr.getOwner().toString());
			if (selectTables == null) {
				throw new SqlParserException(String.format("条件字段%s必须指定表别名", sqlPropertyExpr.getName()));
			}
			selectCondition.setColumnCode(sqlPropertyExpr.getName());
			selectCondition.setColumnAlias(sqlPropertyExpr.getName());
			selectCondition.setTableAlias(selectTables.getTableAlias());
		}
		
		//-验证字段名是否解析完成
		if (StringUtils.isBlank(selectCondition.getColumnCode())) {
			throw new SqlParserException(String.format("条件字段名%s无法解析", conditionSqlExpr.toString()));
		}
		return selectCondition;
	}
	
	/**
	 * 根据表别名获取查询表
	 * @param tableAlias
	 * @return
	 */
	private SelectTable getSelectTable(String tableAlias) {
		int index = selectTables.indexOf(new SelectTable(tableAlias));
		return index >= 0 ? selectTables.get(index) : null;
	}
	
	/**
	 * 根据字段别名获取查询字段
	 * @param columnAlias
	 * @return
	 */
	public SelectColumn getSelectColumn(String columnAlias) {
		int index = selectColumns.indexOf(new SelectColumn(columnAlias));
		return index >= 0 ? selectColumns.get(index) : null;
	}
}
