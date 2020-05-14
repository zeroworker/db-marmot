package db.marmot.graphic.contorller.request;

import db.marmot.graphic.TabGraphic;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
}
