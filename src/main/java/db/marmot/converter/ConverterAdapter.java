package db.marmot.converter;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import db.marmot.enums.*;
import db.marmot.graphic.converter.*;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.convert.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;

import java.lang.reflect.Constructor;
import java.sql.JDBCType;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author shaokang
 */
public class ConverterAdapter {
	
	/**
	 * 字段转换器
	 */
	private static Map<ColumnType, ColumnConverter> COLUMN_CONVERTERS = new HashMap<>();
	
	/**
	 * 运算符转换器
	 */
	private static Map<Operators, OperatorsConverter> OPERATORS_CONVERTERS = new HashMap<>();
	
	/**
	 * 聚合函数转换器
	 */
	private static Map<Aggregates, AggregatesConverter> AGGREGATES_CONVERTERS = new HashMap<>();
	
	/**
	 * 合计转换器
	 */
	private static Map<TotalType, TotalConverter> TOTAL_CONVERTERS = new HashMap<>();
	
	/**
	 * 时间周期转换器
	 */
	private static Map<DateCycle, DateCycleConverter> DATE_CYCLE_CONVERTERS = new HashMap<>();
	
	/**
	 * select sql 转换器
	 */
	private static Map<String, Class> SELECT_SQL_CONVERTERS = new HashMap<>();
	
	/**
	 * 图表类型转换器
	 */
	private static Map<GraphicType, GraphicConverter> GRAPHIC_CONVERTERS = new HashMap<>();
	
	/**
	 * 窗口单位转换器
	 */
	private static Map<WindowUnit, WindowUnitConverter> WINDOW_UNIT_CONVERTERS = new HashMap<>();
	
	/**
	 * 转换适配器
	 */
	private static ConverterAdapter CONVERTER_ADAPTER = new ConverterAdapter();
	
	/**
	 * 获取转换器适配器
	 * @return
	 */
	public static ConverterAdapter getInstance() {
		return CONVERTER_ADAPTER;
	}
	
	public ConverterAdapter() {
		//-注册字段转换器
		registerColumnConverter(new DateColumnConverter());
		registerColumnConverter(new StringColumnConverter());
		registerColumnConverter(new NumberColumnConverter());
		//-注册运算符转换器
		registerOperatorsConverter(new NotEqualsOperatorsConverter());
		registerOperatorsConverter(new EqualsOperatorsConverter());
		registerOperatorsConverter(new GreaterEqualOperatorsConverter());
		registerOperatorsConverter(new GreaterThanOperatorsConverter());
		registerOperatorsConverter(new InOperatorsConverter());
		registerOperatorsConverter(new NotInOperatorsConverter());
		registerOperatorsConverter(new LessEqualOperatorsConverter());
		registerOperatorsConverter(new LessThanOperatorsConverter());
		registerOperatorsConverter(new LikeOperatorsConverter());
		//-聚合函数转换器
		registerAggregatesConverter(new SumAggregatesConverter());
		registerAggregatesConverter(new AvgAggregatesConverter());
		registerAggregatesConverter(new CountAggregatesConverter());
		registerAggregatesConverter(new MaxAggregatesConverter());
		registerAggregatesConverter(new MinAggregatesConverter());
		registerAggregatesConverter(new SumAggregatesConverter());
		registerAggregatesConverter(new CountDistinctAggregatesConverter());
		//-合计转换器
		registerTotalConverter(new SumTotalConverter());
		registerTotalConverter(new MaxTotalConverter());
		registerTotalConverter(new MinTotalConverter());
		registerTotalConverter(new AvgTotalConverter());
		//-时间周期转换器
		registerDateCycleConverter(new DayDateCycleConverter());
		registerDateCycleConverter(new WeekDateCycleConverter());
		registerDateCycleConverter(new MonthDateCycleConverter());
		registerDateCycleConverter(new SeasonDateCycleConverter());
		registerDateCycleConverter(new YearDateCycleConverter());
		
		registerWindowUnitConverter(new SecondWindowUnitConverter());
		registerWindowUnitConverter(new MinuteWindowUnitConverter());
		registerWindowUnitConverter(new HourWindowUnitConverter());
		registerWindowUnitConverter(new DayWindowUnitConverter());
		
		//select sql 转换器器
		registerSelectSqlConverter(DbType.mysql.getCode(), MySqlSelectSqlBuilderConverter.class);
		
		//-图表转换
		registerGraphicConverter(new TabGraphicConverter());
	}
	
	/**
	 * 注册字段转换器
	 * @param columnConverter
	 */
	private void registerColumnConverter(ColumnConverter columnConverter) {
		COLUMN_CONVERTERS.put(columnConverter.columnType(), columnConverter);
	}
	
	/**
	 * 注册运算符转换器
	 * @param operatorsConverter
	 */
	private void registerOperatorsConverter(OperatorsConverter operatorsConverter) {
		OPERATORS_CONVERTERS.put(operatorsConverter.operators(), operatorsConverter);
	}
	
	/**
	 * 注册聚合函数转换器
	 * @param aggregatesConverter
	 */
	private void registerAggregatesConverter(AggregatesConverter aggregatesConverter) {
		AGGREGATES_CONVERTERS.put(aggregatesConverter.aggregates(), aggregatesConverter);
	}
	
	/**
	 * 注册合计转换器
	 * @param totalConverter
	 */
	private void registerTotalConverter(TotalConverter totalConverter) {
		TOTAL_CONVERTERS.put(totalConverter.totalType(), totalConverter);
	}
	
	/**
	 * 注册时间周期转换器
	 * @param dateCycleConverter
	 */
	private void registerDateCycleConverter(DateCycleConverter dateCycleConverter) {
		DATE_CYCLE_CONVERTERS.put(dateCycleConverter.dateCycle(), dateCycleConverter);
	}
	
	/**
	 * 注册select sql 转换器
	 * @param sqlType
	 * @param selectSqlConverter
	 */
	private void registerSelectSqlConverter(String sqlType, Class selectSqlConverter) {
		SELECT_SQL_CONVERTERS.put(sqlType, selectSqlConverter);
	}
	
	/**
	 * 注册图表转换器
	 * @param graphicConverter
	 */
	private void registerGraphicConverter(GraphicConverter graphicConverter) {
		GRAPHIC_CONVERTERS.put(graphicConverter.graphicType(), graphicConverter);
	}
	
	/**
	 * 注册窗口单位转换器
	 * @param windowUnitConverter
	 */
	private void registerWindowUnitConverter(WindowUnitConverter windowUnitConverter) {
		WINDOW_UNIT_CONVERTERS.put(windowUnitConverter.windowUnit(), windowUnitConverter);
	}
	
	/**
	 * 根据数据库字段类型获取字段转换器
	 * @param type
	 * @return
	 */
	public ColumnConverter getColumnConverter(int type) {
		for (ColumnConverter columnConverter : COLUMN_CONVERTERS.values()) {
			List<JDBCType> jdbcTypes = columnConverter.jdbcTypes();
			if (CollectionUtils.isNotEmpty(jdbcTypes)) {
				for (JDBCType jdbcType : jdbcTypes) {
					if (jdbcType.equals(JDBCType.valueOf(type))) {
						return columnConverter;
					}
				}
			}
		}
		throw new ConverterException(String.format("不支持的数据字段类型:%s", type));
	}
	
	/**
	 * 根据数字段类型获取字段转换器
	 * @param columnType
	 * @return
	 */
	public ColumnConverter getColumnConverter(ColumnType columnType) {
		
		Validators.notNull(columnType, "columnType 不能为空");
		
		for (ColumnConverter columnConverter : COLUMN_CONVERTERS.values()) {
			if (columnConverter.columnType().equals(columnType)) {
				return columnConverter;
			}
		}
		throw new ConverterException(String.format("不支持的数据字段类型:%s", columnType.getMessage()));
	}
	
	/**
	 * 根据聚合函数类型获取聚合函数转换器
	 * @param aggregates
	 * @return
	 */
	public AggregatesConverter getAggregatesConverter(Aggregates aggregates) {
		
		Validators.notNull(aggregates, "aggregates 不能为空");
		
		for (AggregatesConverter aggregatesConverter : AGGREGATES_CONVERTERS.values()) {
			if (aggregatesConverter.aggregates().equals(aggregates)) {
				return aggregatesConverter;
			}
			
		}
		throw new ConverterException(String.format("不支持的聚合函数类型:%s", aggregates));
	}
	
	/**
	 * 根据合计类型获取合计转换器
	 * @param totalType
	 * @return
	 */
	public TotalConverter getTotalConverter(TotalType totalType) {
		
		Validators.notNull(totalType, "totalType 不能为空");
		
		for (TotalConverter totalConverter : TOTAL_CONVERTERS.values()) {
			if (totalConverter.totalType().equals(totalType)) {
				return totalConverter;
			}
			
		}
		throw new ConverterException(String.format("不支持的合计类型:%s", totalType));
	}
	
	/**
	 * 根据运算符获取运算符转换器
	 * @param operators
	 * @return
	 */
	public OperatorsConverter getOperatorsConverter(Operators operators) {
		
		Validators.notNull(operators, "operators 不能为空");
		
		for (OperatorsConverter operatorsConverter : OPERATORS_CONVERTERS.values()) {
			if (operatorsConverter.operators().equals(operators)) {
				return operatorsConverter;
			}
			
		}
		throw new ConverterException(String.format("不支持的运算符:%s", operators));
	}
	
	/**
	 * 根据运算符获取运算符转换器
	 * @param operators
	 * @return
	 */
	public OperatorsConverter getOperatorsConverter(SQLBinaryOperator operators) {
		
		Validators.notNull(operators, "operators 不能为空");
		
		for (OperatorsConverter operatorsConverter : OPERATORS_CONVERTERS.values()) {
			SQLBinaryOperator sqlBinaryOperator = operatorsConverter.sqlBinaryOperator();
			if (sqlBinaryOperator != null && sqlBinaryOperator.equals(operators)) {
				return operatorsConverter;
			}
		}
		throw new ConverterException(String.format("不支持的运算符:%s", operators));
	}
	
	/**
	 * 根据图表类型获取图表转换器
	 * @param graphicType
	 * @return
	 */
	public GraphicConverter getGraphicConverter(GraphicType graphicType) {
		
		Validators.notNull(graphicType, "graphicType 不能为空");
		
		for (GraphicConverter graphicConverter : GRAPHIC_CONVERTERS.values()) {
			if (graphicConverter.graphicType().equals(graphicType)) {
				return graphicConverter;
			}
		}
		throw new ConverterException(String.format("不支持的图表类型:%s", graphicType.getMessage()));
	}
	
	/**
	 * 根据周期类型获取周期转换器
	 * @param dateCycle
	 * @return
	 */
	public DateCycleConverter getDateCycleConverter(DateCycle dateCycle) {
		
		Validators.notNull(dateCycle, "dateCycle 不能为空");
		
		for (DateCycleConverter dateCycleConverter : DATE_CYCLE_CONVERTERS.values()) {
			if (dateCycleConverter.dateCycle().equals(dateCycle)) {
				return dateCycleConverter;
			}
		}
		throw new ConverterException(String.format("不支持的周期类型:%s", dateCycle.getMessage()));
	}
	
	/**
	 * 根据窗口粒度获取窗口粒度转换器
	 * @param windowUnit
	 * @return
	 */
	public WindowUnitConverter getWindowUnitConverter(WindowUnit windowUnit) {
		
		Validators.notNull(windowUnit, "windowUnit 不能为空");
		
		for (WindowUnitConverter windowUnitConverter : WINDOW_UNIT_CONVERTERS.values()) {
			if (windowUnitConverter.windowUnit().equals(windowUnit)) {
				return windowUnitConverter;
			}
		}
		throw new ConverterException(String.format("不支持的窗口粒度:%s", windowUnit.getMessage()));
	}
	
	/**
	 * 创建sql构造转换器
	 * @param sqlScript
	 * @return
	 */
	public SelectSqlBuilderConverter newInstanceSqlBuilder(String sqlType, String sqlScript) {
		
		Validators.notNull(sqlType, "sqlType 不能为空");
		
		Class sqlBuilderConverterClasses = SELECT_SQL_CONVERTERS.get(sqlType);
		if (sqlBuilderConverterClasses == null) {
			throw new ConverterException(String.format("%s select sql 转换器未实现", sqlType));
		}
		
		boolean arguments = StringUtils.isNotBlank(sqlScript);
		
		try {
			Constructor<SelectSqlBuilderConverter> constructor = arguments ? sqlBuilderConverterClasses.getConstructor(String.class) : sqlBuilderConverterClasses.getConstructor();
			SelectSqlBuilderConverter selectSqlBuilderConverter = arguments ? constructor.newInstance(sqlScript) : constructor.newInstance();
			selectSqlBuilderConverter.setAggregatesConverters(AGGREGATES_CONVERTERS);
			selectSqlBuilderConverter.setOperatorsConverters(OPERATORS_CONVERTERS);
			selectSqlBuilderConverter.setDateCycleConverters(DATE_CYCLE_CONVERTERS);
			return selectSqlBuilderConverter;
		} catch (Exception e) {
			throw new ConverterException(String.format("%s select sql 转换器实例失败", sqlType), e);
		}
	}
	
	/**
	 * 获取GMT8T时间戳
	 * @param date
	 * @return
	 */
	public Timestamp getGMT8Timestamp(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime());
		int dstOffset = cal.get(Calendar.DST_OFFSET);
		int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
		cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		TimeZone gmt = TimeZone.getTimeZone("GMT+8");
		cal.add(Calendar.MILLISECOND, gmt.getRawOffset() + gmt.getDSTSavings());
		return new Timestamp(cal.getTimeInMillis());
	}
	
	/**
	 * 规则
	 * @param expr 表达式
	 * @return 表达式返回值 {@link Object}
	 */
	public <T> T eval(String expr) {
		return (T) MVEL.eval(expr);
	}
	
	/**
	 * 规则比较器
	 * @param expr 表达式
	 * @param params 参数值
	 * @return 表达式返回值 {@link Object}
	 */
	public <T> T eval(String expr, Map<String, Object> params) {
		return (T) MVEL.eval(expr, params);
	}
	
	public static class Mask {
		public static final char SEPARATOR_CHAR_ASTERISK = '*';
		
		/**
		 * 把字符串mask
		 *
		 * @param str 字符串
		 * @return mask后的字符串
		 */
		public static String mask(String str) {
			if (str == null) {
				return null;
			}
			int len = str.length();
			if (len == 0) {
				return str;
			}
			if (len == 1) {
				return String.valueOf(SEPARATOR_CHAR_ASTERISK);
			}
			int maskLen;
			int begin;
			
			if ((len >= 8 || len <= 11) && str.matches("[A-Z]{1,2}\\d{7,10}")) {
				// 台胞证/回乡证/护照
				// 台胞证上面有两个号码：台胞证号码：0099730503(B) 身份证号码：H125039525
				if (Character.isDigit(str.charAt(1))) {
					begin = 2;
				} else {
					begin = 3;
				}
				maskLen = len - begin - 3;
			} else if ((len == 10 || len == 13) && str.matches("[A-Z]?\\d{8,10}(\\([A-Z]\\))?")) {
				// 台胞证
				// 台胞证上面有两个号码：台胞证号码：0099730503(B) 身份证号码：H125039525
				begin = 2;
				if (str.charAt(str.length() - 1) == ')') {
					maskLen = len - begin - 4;
				} else {
					maskLen = len - begin - 1;
				}
			} else if (len >= 16 && len <= 22) {
				// 卡号位于这个区间，特殊处理，保证前6后4
				maskLen = len - 6 - 4;
				begin = 6;
			} else {
				len = str.length();
				maskLen = Math.max((len) / 2, 1);
				begin = (len - maskLen) / 2;
			}
			return mask(str, begin, begin + maskLen);
		}
		
		/**
		 * 掩码指定的位数为*
		 *
		 * <p>
		 * 注意:index从0开始
		 *
		 * @param str 原字符串
		 * @param beginIndex 开始index,从0开始
		 * @param endIndex 结束index,掩码不包括此位
		 * @return 返回掩码后的字符串
		 */
		public static String mask(String str, int beginIndex, int endIndex) {
			if (str == null || str.length() == 0) {
				return str;
			}
			if (str.length() == 1) {
				return String.valueOf(SEPARATOR_CHAR_ASTERISK);
			}
			
			if (beginIndex < 0) {
				beginIndex = 0;
			}
			if (endIndex > str.length()) {
				endIndex = str.length();
			}
			int subLen = endIndex - beginIndex;
			if (subLen < 0) {
				throw new StringIndexOutOfBoundsException(subLen);
			}
			
			// 复制整个str
			char[] chars = str.toCharArray();
			char[] mask = repeatAsterisk(subLen);
			// 复制mask
			System.arraycopy(mask, 0, chars, beginIndex, subLen);
			// 复制输出
			return new String(chars);
		}
		
		protected static char[] repeatAsterisk(int len) {
			char[] chars = new char[len];
			Arrays.fill(chars, Mask.SEPARATOR_CHAR_ASTERISK);
			return chars;
		}
	}
}
