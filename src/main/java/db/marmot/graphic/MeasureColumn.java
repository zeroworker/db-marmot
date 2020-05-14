package db.marmot.graphic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.google.common.collect.Lists;
import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.ConverterException;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.statistical.AggregateColumn;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.parser.SelectColumn;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class MeasureColumn implements Serializable {
	
	private static final long serialVersionUID = 6710565956641540550L;
	
	/**
	 * 字段排序
	 */
	private int order;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z_]+$", message = "必须是英文字母支持大小写以及下划线")
	private String columnCode;
	
	/**
	 * 字段类型
	 */
	@NotNull
	private ColumnType columnType;
	
	/**
	 * 聚合方式
	 */
	@NotNull
	private Aggregates aggregates = Aggregates.sum;
	
	/**
	 * 统计模型
	 */
	private String modelName;
	
	/**
	 * 计算表达式 四则运算表达式 支持带函数的四则运算 函数表达式表示方式sum@xxxx
	 */
	private String calExpr;
	
	/**
	 * 字段数据格式
	 */
	@NotBlank
	private String dataFormat;
	
	/**
	 * 单位换算 - 乘法 为零 表示不参与计算
	 */
	private double unitValue;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		MeasureColumn that = (MeasureColumn) o;
		return Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode);
	}
	
	/**
	 * 维度字段验证
	 * @param dataVolume 数据集字段
	 * @param aggregateMeasure 是否聚合度量
	 */
	public void validateMeasureColumn(DataVolume dataVolume, boolean aggregateMeasure) {
		if (aggregateMeasure) {
			if (aggregates == Aggregates.non) {
				throw new ValidateException(String.format("度量字段%s聚合方式不能为non", columnCode));
			}
			
			ConverterAdapter.getInstance().getAggregatesConverter(aggregates).validateColumnType(columnType);
			
			try {
				ConverterAdapter.getInstance().getColumnConverter(ColumnType.number).validateColumnFormat(dataFormat);
			} catch (ConverterException converterException) {
				throw new ValidateException(String.format("聚合度量字段%s格式化必须为数字格式化:%s", columnCode, converterException.getMessage()));
			}
			
			//-度量字段类型重置,无论原始字段是什么类型,在获取数据后度量字段永远是数字
			columnType = ColumnType.number;
		}
		
		if (!aggregateMeasure) {
			if (aggregates != Aggregates.non) {
				throw new ValidateException(String.format("度量字段%s聚合函数必须为non", columnCode));
			}
			
			if (columnType != ColumnType.number) {
				throw new ValidateException(String.format("度量字段%s类型必须为数字", columnCode));
			}
			
			ConverterAdapter.getInstance().getColumnConverter(columnType).validateColumnFormat(dataFormat);
		}
		
		if (StringUtils.isNotBlank(calExpr)) {
			if (aggregates != Aggregates.calculate) {
				throw new ValidateException(String.format("度量字段%s存在计算表达式，聚合类型必须为calculate", columnCode));
			}
			validateMeasureColumnExpr(dataVolume, aggregateMeasure);
			return;
		}
		
		DataColumn dataColumn = dataVolume.findDataColumn(columnCode);
		if (dataColumn == null) {
			throw new ValidateException(String.format("度量字段%s在数据集字段中不存在", columnCode));
		}
		if (dataColumn.getColumnType() != columnType) {
			throw new ValidateException(String.format("度量字段%s字段类型与数据集字段类型不匹配", columnCode));
		}
		
		unitValue = dataColumn.getUnitValue();
	}
	
	/**
	 * 验证度量字段计算表达式
	 * @param dataVolume 数据集
	 * @param aggregateMeasure 是否聚合度量
	 */
	private void validateMeasureColumnExpr(DataVolume dataVolume, boolean aggregateMeasure) {
		Map<String, Aggregates> aggregateVariables = parseExprVariable();
		for (String variableColumn : aggregateVariables.keySet()) {
			DataColumn dataColumn = dataVolume.findDataColumn(variableColumn);
			if (dataColumn == null) {
				throw new ValidateException(String.format("度量字段%s计算表达式变量%s在数据集字段中不存在", variableColumn));
			}
			Aggregates aggregates = aggregateVariables.get(variableColumn);
			if (aggregateMeasure) {
				if (aggregates == Aggregates.non) {
					throw new ValidateException(String.format("度量字段%s计算表达式变量%s必须为聚合计算变量", columnCode, variableColumn));
				}
				if (aggregates == Aggregates.count_distinct) {
					throw new ValidateException(String.format("度量字段%s计算表达式变量%s不支持去重聚合计算", columnCode, variableColumn));
				}
				ConverterAdapter.getInstance().getAggregatesConverter(aggregates).validateColumnType(dataColumn.getColumnType());
			}
			if (!aggregateMeasure) {
				if (aggregates != Aggregates.non) {
					throw new ValidateException(String.format("度量字段%s计算表达式变量%s不能存在聚合函数", columnCode, variableColumn));
				}
				if (dataColumn.getColumnType() != ColumnType.number) {
					throw new ValidateException(String.format("度量字段%s计算表达式变量%s类型必须为数字", columnCode, variableColumn));
				}
			}
		}
	}
	
	/**
	 * 将表达式转换成sql表达式
	 * @param dataVolume 数据集
	 * @return
	 */
	public String convertSqlExpr(DataVolume dataVolume) {
		String expr = calExpr;
		Map<String, Aggregates> aggregateVariables = parseExprVariable();
		for (String variableColumn : aggregateVariables.keySet()) {
			Aggregates aggregates = aggregateVariables.get(variableColumn);
			if (aggregates != Aggregates.non) {
				expr = expr.replace(aggregates.getCode() + "@" + variableColumn, aggregates.getCode() + "(" + variableColumn + ")");
			}
			DataColumn dataColumn = dataVolume.findDataColumn(variableColumn);
			if (dataColumn.getUnitValue() > 0) {
				expr = expr.replace(dataColumn.getColumnCode(), dataColumn.getColumnCode() + " * " + dataColumn.getUnitValue());
			}
		}
		return expr;
	}
	
	/**
	 * 获取计算表达式变量
	 * @return
	 */
	public Map<String, Aggregates> parseExprVariable() {
		Map<String, Aggregates> aggregateVariables = new HashMap<>();
		if (StringUtils.isNotBlank(calExpr)) {
			List<String> variables = Lists.newArrayList();
			Matcher exprMatcher = java.util.regex.Pattern.compile("[^\\(\\)|\\*|\\/|\\-|\\+\\s]+").matcher(calExpr);
			while (exprMatcher.find()) {
				variables.add(exprMatcher.group());
			}
			for (String variable : variables) {
				String regex = "sum@+[a-zA-Z_]+|count@+[a-zA-Z_]+|max@+[a-zA-Z_]+|min@+[a-zA-Z_]+|avg@+[a-zA-Z_]+|count_distinct@+[a-zA-Z_]+";
				Matcher variableMatcher = java.util.regex.Pattern.compile(regex).matcher(variable);
				if (variableMatcher.find()) {
					String[] str = variable.split("@");
					aggregateVariables.put(str[1], Aggregates.getByCode(str[0]));
					continue;
				}
				if (!NumberUtils.isParsable(variable)) {
					aggregateVariables.put(variable, Aggregates.non);
				}
			}
		}
		return aggregateVariables;
	}
	
	/**
	 * 设置统计字段
	 * @param statisticalModel
	 * @param selectColumn
	 * @param dataVolume
	 */
	public void addAggregateColumn(StatisticalModel statisticalModel, DataVolume dataVolume, SelectColumn selectColumn) {
		//-计算字段
		if (aggregates == Aggregates.calculate) {
			Map<String, Aggregates> aggregateVariables = parseExprVariable();
			aggregateVariables.forEach((variableCode, aggregates) -> {
				DataColumn dataColumn = dataVolume.findDataColumn(variableCode);
				if (dataColumn.getColumnCode().equals(selectColumn.getColumnAlias())) {
					//-设置模型名
					modelName = StringUtils.isBlank(modelName) ? statisticalModel.getModelName() : StringUtils.join(modelName, ",", statisticalModel.getModelName());
					//-添加统计字段
					statisticalModel.getAggregateColumns().add(new AggregateColumn(selectColumn.getColumnAlias(), dataColumn.getColumnType(), aggregates));
				}
			});
		} else {
			//-非计算字段
			if (columnCode.equals(selectColumn.getColumnAlias())) {
				//-设置统计模型名
				modelName = statisticalModel.getModelName();
				//-设置统计值
				statisticalModel.getAggregateColumns().add(new AggregateColumn(columnCode, columnType, aggregates));
			}
		}
	}
	
}
