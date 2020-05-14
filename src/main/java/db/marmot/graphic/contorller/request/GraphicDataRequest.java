package db.marmot.graphic.contorller.request;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class GraphicDataRequest<G extends Graphic> {
	
	/**
	 * 数据集ID
	 */
	private long volumeId;
	
	/**
	 * 创建人ID
	 */
	private String founderId;
	
	/**
	 * 图表名称
	 */
	private String graphicName;
	
	/**
	 * 图表类型
	 */
	@NotNull
	private GraphicType graphicType;
	
	/**
	 * 获取图表
	 * @return
	 */
	public abstract G getGraphic();

	@Override
	public String toString() {
		return new StringJoiner(", ", GraphicDataRequest.class.getSimpleName() + "[", "]")
				.add("volumeId=" + volumeId)
				.add("founderId='" + founderId + "'")
				.add("graphicName='" + graphicName + "'")
				.add("graphicType=" + graphicType)
				.toString();
	}
}
