package db.marmot.graphic.generator;

import db.marmot.enums.ColumnType;
import db.marmot.enums.DataColor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class GraphicDataColumn implements Serializable {
	
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
	 * 字段数据格式
	 */
	private String dataFormat;
	
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
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GraphicDataColumn that = (GraphicDataColumn) o;
		return Objects.equals(columnCode, that.columnCode);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(columnCode);
	}
}
