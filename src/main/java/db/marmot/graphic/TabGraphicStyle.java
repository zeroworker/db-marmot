package db.marmot.graphic;

import db.marmot.enums.TotalType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicStyle extends GraphicStyle {
	
	private static final long serialVersionUID = 1035090345660306256L;
	
	/**
	 * 是否显示序号
	 */
	private boolean serialNum = false;
	
	/**
	 * 是否合并相同列
	 */
	private boolean mergeColumn = false;
	
	/**
	 * 是否层级列
	 */
	private boolean rankColumn = false;
	
	/**
	 * 是否冻结列
	 */
	private boolean freezeColumn = false;
	
	/**
	 * 冻结列数-从第一列计算
	 */
	private int startFreezeNum = 0;
	
	/**
	 * 冻结列数-从最后一列计算
	 */
	private int endFreezeNum = 0;
	
	/**
	 * 字段无值填充值
	 */
	@NotBlank
	private String paddedValue = "/";
	
	/**
	 * 是否列合计
	 */
	private boolean columnTotal = false;
	
	/**
	 * 列合计别名
	 */
	private String columnTotalAlias = "合计";
	
	/**
	 * 列合计方式
	 */
	@Valid
	@NotNull
	private List<TotalColumn> totalColumns = new ArrayList<>();
	
	/**
	 * 是否列小计
	 */
	private boolean columnSubtotal = false;
	
	/**
	 * 小计别名
	 */
	private String subtotalAlias = "小计";
	
	/**
	 * 是否行合计-行合计针对存在列转行时生效 其他场景不生效
	 */
	private boolean rowTotal = false;
	
	/**
	 * 行合计方式
	 */
	private TotalType rowTotalType = TotalType.sum;
	
	/**
	 * 行合计别名
	 */
	private String rowTotalAlias = "合计";
	
	/**
	 * 字段样式
	 */
	@Valid
	@NotNull
	private List<TabColumnStyle> tabColumnStyles = new ArrayList<>();
	
	/**
	 * 根据字段名获取字段样式
	 * @param columnCode
	 * @return
	 */
	public TabColumnStyle findTabColumnStyle(String columnCode) {
		for (TabColumnStyle tabColumnStyle : tabColumnStyles) {
			if (tabColumnStyle.getColumnCode().equals(columnCode)) {
				return tabColumnStyle;
			}
		}
		return null;
	}
	
	/**
	 * 根据字段名获取合计列
	 * @param columnCode
	 * @return
	 */
	public TotalColumn findTotalColumn(String columnCode) {
		for (TotalColumn totalColumn : totalColumns) {
			if (totalColumn.getColumnCode().equals(columnCode)) {
				return totalColumn;
			}
		}
		return null;
	}
	
	/**
	 * 计算列是否固定
	 * @param columnIndex 当前列角标位
	 * @param columnNum 总列数
	 * @return
	 */
	public boolean calculateFreeze(int columnIndex, int columnNum) {
		if (freezeColumn && startFreezeNum > 0 && columnIndex > 0 && columnIndex <= startFreezeNum) {
			return true;
		}
		if (freezeColumn && endFreezeNum > 0 && columnIndex >= endFreezeNum && columnIndex <= columnNum) {
			return true;
		}
		return false;
	}
	
	@Override
	public void validateGraphicStyle() {
		Validators.assertJSR303(this);
		if (columnSubtotal && StringUtils.isBlank(subtotalAlias)) {
			throw new ValidateException("小计别名不能为空");
		}
		if (rowTotal) {
			if (rowTotalType == null) {
				throw new ValidateException("行合计方式不能为空");
			}
			if (StringUtils.isBlank(rowTotalAlias)) {
				throw new ValidateException("行合计别名不能为空");
			}
		}
		if (columnTotal && StringUtils.isBlank(columnTotalAlias)) {
			throw new ValidateException("列合计别名不能为空");
		}
	}
}
