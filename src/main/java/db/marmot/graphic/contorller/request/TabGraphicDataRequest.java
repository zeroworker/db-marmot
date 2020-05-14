package db.marmot.graphic.contorller.request;

import db.marmot.graphic.TabGraphic;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicDataRequest extends GraphicDataRequest<TabGraphic> {
	
	/**
	 * 表格图表
	 */
	@NotNull
	private TabGraphic graphic;
	
	@Override
	public TabGraphic getGraphic() {
		return this.graphic;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TabGraphicDataRequest.class.getSimpleName() + "[", "]")
				.add("graphic=" + graphic)
				.toString();
	}
}
