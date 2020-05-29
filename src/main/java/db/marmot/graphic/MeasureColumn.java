package db.marmot.graphic;

import com.google.common.collect.Lists;
import db.marmot.converter.ColumnConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

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
	 * 计算表达式 四则运算表达式 支持带函数的四则运算 函数表达式表示方式sum@xxxx
	 */
	private String calExpr;
	
	/**
	 * 字段数据格式
	 */
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
	 */
	public void validateMeasureColumn(DataVolume dataVolume) {
		if (aggregates != Aggregates.non && aggregates != Aggregates.calculate) {
			ConverterAdapter.getInstance().getAggregatesConverter(aggregates).validateColumnType(columnType);
		}
		if (aggregates == Aggregates.calculate) {
			validateMeasureColumnExpr(dataVolume);
		}
		DataColumn dataColumn = dataVolume.findDataColumn(columnCode, columnType);
		this.unitValue = dataColumn.getUnitValue();
		ColumnConverter columnConverter = ConverterAdapter.getInstance().getColumnConverter(ColumnType.number);
		if (StringUtils.isNotBlank(dataFormat)) {
			columnConverter.validateColumnFormat(dataFormat);
			return;
		}
		this.dataFormat = columnConverter.defaultDataFormat();
	}
	
	/**
	 * 验证度量字段计算表达式
	 * @param dataVolume 数据集
	 */
	private void validateMeasureColumnExpr(DataVolume dataVolume) {
		Map<String, Aggregates> aggregateVariables = parseExprVariable();
		if (MapUtils.isNotEmpty(aggregateVariables)) {
			aggregateVariables.forEach((variableColumn, aggregates) -> {
				DataColumn dataColumn = dataVolume.findDataColumn(variableColumn, null);
				ConverterAdapter.getInstance().getAggregatesConverter(aggregates).validateColumnType(dataColumn.getColumnType());
			});
		}
	}
	
	/**
	 * 将表达式转换成sql表达式
	 * @param dataVolume 数据集
	 * @return
	 */
	public String convertSqlExpr(DataVolume dataVolume) {
		String expr = this.calExpr;
		Map<String, Aggregates> aggregateVariables = parseExprVariable();
		if (MapUtils.isNotEmpty(aggregateVariables)) {
			for (String variableColumn : aggregateVariables.keySet()) {
				Aggregates aggregates = aggregateVariables.get(variableColumn);
				if (aggregates != Aggregates.non) {
					expr = expr.replace(aggregates.getCode() + "@" + variableColumn, aggregates.getCode() + "(" + variableColumn + ")");
				}
				DataColumn dataColumn = dataVolume.findDataColumn(variableColumn, null);
				if (dataColumn.getUnitValue() > 0) {
					expr = expr.replace(dataColumn.getColumnCode(), dataColumn.getColumnCode() + " * " + dataColumn.getUnitValue());
				}
			}
		}
		return expr;
	}
	
	/**
	 * 获取计算表达式变量
	 * @return
	 */
	public Map<String, Aggregates> parseExprVariable() {
		if (StringUtils.isNotBlank(calExpr)) {
			Map<String, Aggregates> aggregateVariables = new HashMap<>();
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
			return aggregateVariables;
		}
		return null;
	}
}
