package db.marmot.graphic.generator;

import db.marmot.enums.TotalType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicDataColumn extends GraphicDataColumn {
	
	private static final long serialVersionUID = -4388517356266092861L;
	
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
	 * 字段层级展示行
	 */
	private List<TabGraphicRank> tabGraphicRanks = new ArrayList<>();
	
	/**
	 * 添加列层级行
	 * @param startRow
	 * @param endRow
	 * @return
	 */
	public void addTabGraphicRank(int startRow, int endRow) {
		tabGraphicRanks.add(new TabGraphicRank(startRow, endRow));
	}
}
