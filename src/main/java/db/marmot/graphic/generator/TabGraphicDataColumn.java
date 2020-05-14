package db.marmot.graphic.generator;

import java.util.*;

import db.marmot.enums.ColumnType;
import db.marmot.enums.DataColor;
import db.marmot.enums.TotalType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicDataColumn {
	
	/**
	 * 字段编码-该字段编码当前列最后一行的字段编码
	 */
	private String columnCode;
	
	/**
	 * 字段类型
	 */
	private ColumnType columnType;
	
	/**
	 * 数据颜色
	 */
	private DataColor dataColor;
	
	/**
	 * 是否维度字段
	 */
	private boolean dimenColumn = true;
	
	/**
	 * 是否合并列
	 */
	private boolean mergeColumn = false;
	
	/**
	 * 是否冻结列
	 */
	private boolean freezeColumn = false;
	
	/**
	 * 是否行合计
	 */
	private boolean rowTotal = false;
	
	/**
	 * 是否列小计
	 */
	private boolean columnTotal = false;
	
	/**
	 * 列合计方式
	 */
	private TotalType columnTotalType;
	
	/**
	 * 是否列小计
	 */
	private boolean columnSubtotal = false;
	
	/**
	 * 字段数据格式
	 */
	private String dataFormat;
	
	/**
	 * 字段层级展示行
	 */
	private List<TabGraphicRank> tabGraphicRanks = new ArrayList<>();
	
	/**
	 * 列数据行表头 map.key 为字段编码,map.value 字段名称
	 */
	private List<Map<String, String>> rowColumns = new ArrayList<>();
	
	/**
	 * 添加行字段
	 * @param columnCode
	 * @param columnName
	 * @return
	 */
	public void addRowColumn(String columnCode, String columnName) {
		Map<String, String> rowColumn = new HashMap<>();
		rowColumn.put(columnCode, columnName);
		rowColumns.add(rowColumn);
	}
	
	/**
	 * 添加列层级行
	 * @param startRow
	 * @param endRow
	 * @return
	 */
	public void addTabGraphicRank(int startRow, int endRow) {
		tabGraphicRanks.add(new TabGraphicRank(startRow, endRow));
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TabGraphicDataColumn that = (TabGraphicDataColumn) o;
		return Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode);
	}
}
