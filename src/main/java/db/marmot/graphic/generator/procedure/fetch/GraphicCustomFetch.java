package db.marmot.graphic.generator.procedure.fetch;

import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.repository.DataSourceRepository;

/**
 * @author shaokang
 */
public abstract class GraphicCustomFetch<G extends Graphic, D extends GraphicData> implements GraphicFetch<G,D>{

    protected DataSourceRepository dataSourceRepository;

    public GraphicCustomFetch(DataSourceRepository dataSourceRepository) {
        this.dataSourceRepository = dataSourceRepository;
    }
}
