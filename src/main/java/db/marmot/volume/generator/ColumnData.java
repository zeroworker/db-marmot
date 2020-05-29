package db.marmot.volume.generator;

import db.marmot.volume.DataColumn;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
@Setter
@Getter
public class ColumnData implements Serializable {
	
	private static final long serialVersionUID = -2502863146259717109L;
	
	/**
	 * 脚本
	 */
	private String script;

	/**
	 * 字段值编码
	 */
	private String columnValueCode;
	
	/**
	 * 字段展示编码
	 */
	private String columnShowCode;
	
	/**
	 * 数据集字段
	 */
	private List<DataColumn> columns = new ArrayList<>();
	
	/**
	 * 数据集数据
	 */
	private List<Map<String, Object>> data = new ArrayList<>();

	public ColumnData(String script,String columnValueCode, String columnShowCode) {
		this.script = script;
		this.columnValueCode = columnValueCode;
		this.columnShowCode = columnShowCode;
	}

	public ColumnData(String columnValueCode, String columnShowCode) {
		this.columnValueCode = columnValueCode;
		this.columnShowCode = columnShowCode;
	}

	public DataColumn findDataColumn(String columnCode) {
		for (DataColumn dataColumn : columns) {
			if (dataColumn.getColumnCode().equals(columnCode)) {
				return dataColumn;
			}
		}
		return null;
	}
}
