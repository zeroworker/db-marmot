package db.marmot.graphic;

import db.marmot.repository.RepositoryFactoryBean;

/**
 * @author shaokang
 */
public class GraphicRepositoryBean extends RepositoryFactoryBean<GraphicRepository> {
	
	@Override
	public GraphicRepository newInstance() {
		return new GraphicRepository(getTemplates());
	}
}
