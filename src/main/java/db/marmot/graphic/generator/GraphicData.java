package db.marmot.graphic.generator;

import com.google.common.collect.Lists;
import db.marmot.converter.ColumnConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.GraphicLayout;
import db.marmot.enums.GraphicType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class GraphicData implements Serializable {

	private static final long serialVersionUID = -2086080708445869859L;
	
	/**
	 * 图表sql
	 */
	private String graphicSql;
	
	/**
	 * 图表类型
	 */
	private GraphicType graphicType;
	
	/**
	 * 图表格式
	 */
	private GraphicLayout graphicLayout;
	
	/**
	 * 图表描述 图表结果描述
	 */
	private String graphicMemo = "生成成功";
	
	/**
	 * 图表数据
	 */
	private List<Map<String, Object>> data = new ArrayList<>();
	
	/**
	 * 图表数据是否为空
	 * @return
	 */
	public boolean emptyData() {
		return CollectionUtils.isNotEmpty(data);
	}
	
	/**
	 * 格式化数据
	 */
	public void formatValueGraphicData() {
		List<GraphicDataColumn> graphicDataColumns = getGraphicDataColumns();
		data.forEach(rowData -> {
			for (String columnCode : rowData.keySet()) {
				Object columnValue = rowData.get(columnCode);
				for (GraphicDataColumn graphicDataColumn : graphicDataColumns) {
					ColumnConverter columnConverter = ConverterAdapter.getInstance().getColumnConverter(graphicDataColumn.getColumnType());
					if (columnConverter.validateColumnValue(columnValue.getClass())) {
						rowData.put(columnCode, columnConverter.formatColumnValue(columnValue, graphicDataColumn.getDataFormat()));
					}
				}
			}
		});
	}
	
	public List<List<String>> buildFileHead() {
		List<List<String>> heads = Lists.newArrayList();
		List<GraphicDataColumn> graphicDataColumns = getGraphicDataColumns();
		graphicDataColumns.forEach(tabGraphicColumn -> {
			List<String> rowHeads = new ArrayList<>();
			tabGraphicColumn.getRowColumns().forEach(head -> rowHeads.addAll(head.values().stream().collect(Collectors.toList())));
			heads.add(rowHeads);
		});
		return heads;
	}
	
	public List<List<Object>> buildFileData() {
		List<List<Object>> fileData = new ArrayList<>();
		data.forEach(rowData -> {
			List rowDataValue = rowData.values().stream().collect(Collectors.toList());
			fileData.addAll(rowDataValue);
		});
		return fileData;
	}
	
	public abstract <C extends GraphicDataColumn> List<C> getGraphicDataColumns();
}
