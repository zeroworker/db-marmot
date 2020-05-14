package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.converter.ConverterAdapter;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.volume.DataBaseRepository;

/**
 * @author shaokang
 */
public abstract class GraphicSqlFetch<G extends Graphic, D extends GraphicData> implements GraphicFetch<G, D> {
	
	protected DataBaseRepository dataBaseRepository;
	
	protected ConverterAdapter converterAdapter = ConverterAdapter.getInstance();

	public GraphicSqlFetch(DataBaseRepository dataBaseRepository) {
		this.dataBaseRepository = dataBaseRepository;
	}
}
