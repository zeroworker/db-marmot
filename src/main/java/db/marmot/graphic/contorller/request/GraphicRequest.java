package db.marmot.graphic.contorller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

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
	private String graphicCode;

	@Override
	public String toString() {
		return new StringJoiner(", ", GraphicRequest.class.getSimpleName() + "[", "]")
				.add("founderId='" + founderId + "'")
				.add("graphicCode='" + graphicCode + "'")
				.toString();
	}
}
