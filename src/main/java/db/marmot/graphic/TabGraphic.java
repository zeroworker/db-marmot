package db.marmot.graphic;

import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphic extends Graphic {
	
	private static final long serialVersionUID = 5362635861125108894L;

	/**
	 * 表格样式
	 */
	@NotNull
	private TabGraphicStyle graphicStyle = new TabGraphicStyle();
	
	@Override
	public void validateGraphic(DataVolume dataVolume) {
		graphicStyle.validateGraphicStyle();
		super.validateGraphic(dataVolume);
	}
}
