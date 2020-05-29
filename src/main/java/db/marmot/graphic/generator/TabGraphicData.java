package db.marmot.graphic.generator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicData extends GraphicData{
	
	private static final long serialVersionUID = -5288435005429513768L;
	
	/**
	 * 表格显示序号
	 */
	private boolean serialNum;
	
	/**
	 * 表格合并相同列
	 */
	private boolean mergeColumn;
	
	/**
	 * 是否列合计
	 */
	private boolean columnTotal;
	
	/**
	 * 是否行合计
	 */
	private boolean rowTotal;
	
	/**
	 * 表格层级列
	 */
	private boolean rankColumn;
	
	/**
	 * 表格列 每一个element代表一列数据的字段以及字段样式,每列数据存在多行字段
	 */
	private List<TabGraphicDataColumn> graphicDataColumns = new ArrayList<>();
	
	@Override
	public List<TabGraphicDataColumn> getGraphicDataColumns() {
		return graphicDataColumns;
	}
	
	public void addTabColumn(TabGraphicDataColumn tabGraphicColumn) {
		graphicDataColumns.add(tabGraphicColumn);
	}
}
