package db.marmot.converter;

import db.marmot.enums.*;
import db.marmot.graphic.converter.*;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.convert.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.sql.JDBCType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static Map<GraphicCycle, GraphicCycleConverter> GRAPHIC_CYCLE_CONVERTERS = new HashMap<>();
	
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
	
	private ConverterAdapter() {
		if (CONVERTER_ADAPTER != null) {
			throw new ConverterException("converterAdapter 不允许实例化");
		}
		
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
		//-图表周期转换器
		registerGraphicCycleConverter(new SecondGraphicCycleConverter());
		registerGraphicCycleConverter(new MinuteGraphicCycleConverter());
		registerGraphicCycleConverter(new HourGraphicCycleConverter());
		registerGraphicCycleConverter(new DayGraphicCycleConverter());
		registerGraphicCycleConverter(new WeekGraphicCycleConverter());
		registerGraphicCycleConverter(new MonthGraphicCycleConverter());
		registerGraphicCycleConverter(new SeasonGraphicCycleConverter());
		registerGraphicCycleConverter(new YearGraphicCycleConverter());
		//-统计窗口转换器
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
	 * 注册图表周期转换器
	 * @param graphicCycleConverter
	 */
	private void registerGraphicCycleConverter(GraphicCycleConverter graphicCycleConverter) {
		GRAPHIC_CYCLE_CONVERTERS.put(graphicCycleConverter.graphicCycle(), graphicCycleConverter);
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
	 * @param graphicCycle
	 * @return
	 */
	public GraphicCycleConverter getGraphicCycleConverter(GraphicCycle graphicCycle) {
		
		Validators.notNull(graphicCycle, "graphicCycle 不能为空");
		
		for (GraphicCycleConverter graphicCycleConverter : GRAPHIC_CYCLE_CONVERTERS.values()) {
			if (graphicCycleConverter.graphicCycle().equals(graphicCycle)) {
				return graphicCycleConverter;
			}
		}
		throw new ConverterException(String.format("不支持的周期类型:%s", graphicCycle.getMessage()));
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
		Validators.notNull(sqlBuilderConverterClasses, "%s select sql 转换器未实现", sqlType);
		boolean arguments = StringUtils.isNotBlank(sqlScript);
		try {
			Constructor<SelectSqlBuilderConverter> constructor = arguments ? sqlBuilderConverterClasses.getConstructor(String.class) : sqlBuilderConverterClasses.getConstructor();
			SelectSqlBuilderConverter selectSqlBuilderConverter = arguments ? constructor.newInstance(sqlScript) : constructor.newInstance();
			selectSqlBuilderConverter.setAggregatesConverters(AGGREGATES_CONVERTERS);
			selectSqlBuilderConverter.setOperatorsConverters(OPERATORS_CONVERTERS);
			selectSqlBuilderConverter.setGraphicCycleConverters(GRAPHIC_CYCLE_CONVERTERS);
			return selectSqlBuilderConverter;
		} catch (Exception e) {
			throw new ConverterException(String.format("%s select sql 转换器实例失败", sqlType), e);
		}
	}
}
