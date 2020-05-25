package db.marmot.volume;

import db.marmot.enums.ColumnType;
import db.marmot.repository.validate.Validators;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * 数据字段
 * @author shaokang
 */
@Setter
@Getter
public class DataColumn {
	
	/**
	 * 字段ID
	 */
	private long columnId;
	
	/**
	 * 数据集ID
	 */
	private String volumeCode;
	
	/**
	 * 字段顺序
	 */
	private int columnOrder;
	
	/**
	 * 字段名
	 */
	@Size(max = 512)
	private String columnName;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	@Size(max = 512)
	@Pattern(regexp = "^[a-zA-Z_]+$", message = "必须是英文字母支持大小写以及下划线")
	private String columnCode;
	
	/**
	 * 字段类型
	 */
	@NotNull
	private ColumnType columnType;
	
	/**
	 * <li>字段标记 用于标记字段 使用场景：用户使用数据集字段生成数据时，根据用户识别的出该字段必须使用于
	 * 过滤，分组。但无需用户选择时使用,业务逻辑处理。该标签自定义</li>
	 * <li>字段标签若不存在,表示用户可以选择该字段,字段标签存在,于用户匹配的标签对应的字段无需用户选择,逻辑处理,也无需展示</li>
	 * <li>根据具体业务场景,可自行穷举枚举</li>
	 * <li>例：商户用户登陆，图表过滤、分组必须存在商户ID,merchant（商户标记）,department（部门标记）</li>
	 * <li>ps:按道理说
	 * 这些字段无需增加标记,业务场景是可以识别的,一般需要增加标记的字段都是固定的业务属性字段,字段编码应该是保持一致的，这里增加上算是冗余
	 * 可以不使用</li>
	 */
	@Size(min = 128)
	private String columnLabel;
	
	/**
	 * 筛选字段 数据过滤时 显示字段和筛选字段非同一个字段时使用 该筛选字段必须在数据集对应sql中体现 默认和columnCode保持一致
	 */
	@NotNull
	@Size(max = 512)
	private String screenColumn;
	
	/**
	 * 字段过滤- 数据过滤时,字段是否可参与过滤
	 */
	private boolean columnFilter = true;
	
	/**
	 * 字段隐藏-在做数据仪表盘时不显示
	 */
	private boolean columnHidden = false;
	
	/**
	 * 是否需要字段转换 例如 字段值枚举code 需要转换成message
	 */
	private boolean columnEscape = false;
	
	/**
	 * 是否为角标字段,用于模型统计时抓取数据角标定位
	 */
	private boolean columnIndex = false;
	
	/**
	 * 字段掩码-针对敏感字段掩码处理
	 */
	private boolean columnMask = false;
	
	/**
	 * 数据格式 时间 数字
	 */
	@NotNull
	@Size(max = 512)
	private String dataFormat;
	
	/**
	 * 单位换算 - 乘法 为零 表示不参与计算
	 */
	private double unitValue;
	
	/**
	 * 描述
	 */
	@Size(max = 512)
	private String content;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DataColumn that = (DataColumn) o;
		return volumeCode == that.volumeCode && Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(volumeCode, columnCode);
	}
	
	public void validateDataColumn() {
		Validators.assertJSR303(this);
	}
}
