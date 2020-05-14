package db.marmot.graphic.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicRank {
	
	private int startRow;
	private int endRow;
	
	public TabGraphicRank(int startRow, int endRow) {
		this.startRow = startRow;
		this.endRow = endRow;
	}
}
