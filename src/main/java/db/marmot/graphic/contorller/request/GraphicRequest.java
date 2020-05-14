package db.marmot.graphic.contorller.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GraphicRequest {
	
	/**
	 * 创建人ID
	 */
	private String founderId;
	
	/**
	 * 图表ID
	 */
	private long graphicId;
}
