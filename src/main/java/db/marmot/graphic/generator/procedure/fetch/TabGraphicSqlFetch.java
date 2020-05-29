package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataBaseRepository;

/**
 * @author shaokang
 */
public class TabGraphicSqlFetch extends GraphicSqlFetch<TabGraphic, TabGraphicData> {

	public TabGraphicSqlFetch(DataBaseRepository dataBaseRepository) {
		super(dataBaseRepository);
	}
}
