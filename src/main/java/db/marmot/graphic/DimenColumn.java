package db.marmot.graphic;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.ColumnType;
import db.marmot.enums.DateCycle;
import db.marmot.enums.OrderType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.statistical.GroupColumn;
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
public class DimenColumn implements Serializable {
	
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
	private ColumnType columnType = ColumnType.string;
	
	/**
	 * 字段数据格式
	 */
	@NotBlank
	private String dataFormat;
	
	/**
	 * 时间周期
	 */
	@NotNull
	private DateCycle dateCycle = DateCycle.non;
	
	/**
	 * 排序方式
	 */
	@NotNull
	private OrderType orderType = OrderType.asc;
	
	/**
	 * 单位换算 - 乘法 为零 表示不参与计算
	 */
	private double unitValue;
	
	/**
	 * 是否需要字段转换 例如 字段值枚举code 需要转换成message
	 */
	private boolean columnEscape = false;
	
	/**
	 * 字段掩码-针对敏感字段掩码处理
	 */
	private boolean columnMask = false;
	
	/**
	 * 是否列转行
	 */
	private boolean columnToRow = false;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		DimenColumn that = (DimenColumn) o;
		return Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode);
	}
	
	/**
	 * 匹配周期字段
	 * @return
	 */
	public boolean mathCycleColumn() {
		return columnType == ColumnType.date && dateCycle != DateCycle.non;
	}
	
	/**
	 * 验证维度字段
	 * @param dataVolume
	 * @param aggregateDimen
	 */
	public void validateDimenColumn(DataVolume dataVolume, boolean aggregateDimen) {
		
		if (aggregateDimen && dateCycle == DateCycle.non) {
			throw new ValidateException(String.format("聚合维度字段周期不能为non", this.columnCode));
		}
		
		if (!aggregateDimen && dateCycle != DateCycle.non) {
			throw new ValidateException(String.format("明细字段周期必须为non", this.columnCode));
		}
		
		DataColumn dataColumn = dataVolume.findDataColumn(this.getColumnCode());
		if (dataColumn == null) {
			throw new ValidateException(String.format("维度字段%s在数据集字段中不存在", this.columnCode));
		}
		
		if (columnType != dataColumn.getColumnType()) {
			throw new ValidateException(String.format("维度字段%s字段类型与数据集字段类型不匹配", columnCode));
		}
		
		ConverterAdapter.getInstance().getColumnConverter(columnType).validateColumnFormat(dataFormat);
		
		this.unitValue = dataColumn.getUnitValue();
		this.columnMask = dataColumn.isColumnMask();
		this.columnEscape = dataColumn.isColumnEscape();
	}
	
	/**
	 * 添加分组字段
	 * @param statisticalModel
	 */
	public void addGroupColumn(StatisticalModel statisticalModel, SelectColumn selectColumn) {
		if (columnCode.equals(selectColumn.getColumnAlias())){
			statisticalModel.getGroupColumns().add(new GroupColumn(columnCode,columnType));
		}
	}
	
}
