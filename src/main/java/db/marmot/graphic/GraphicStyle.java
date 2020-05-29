package db.marmot.graphic;

import java.io.Serializable;

/**
 * @author shaokang
 */
public abstract class GraphicStyle implements Serializable {
	
	/**
	 * 校验图表样式
	 */
	abstract void validateGraphicStyle();
}
