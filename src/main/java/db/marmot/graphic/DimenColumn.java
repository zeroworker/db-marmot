package db.marmot.graphic;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.ColumnType;
import db.marmot.enums.OrderType;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

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
	 * 验证维度字段
	 * @param dataVolume
	 */
	public void validateDimenColumn(DataVolume dataVolume) {
		ConverterAdapter.getInstance().getColumnConverter(columnType).validateColumnFormat(dataFormat);
		DataColumn dataColumn = dataVolume.findDataColumn(columnCode, columnType);
		this.unitValue = dataColumn.getUnitValue();
		this.columnMask = dataColumn.isColumnMask();
		this.columnEscape = dataColumn.isColumnEscape();
	}
}
