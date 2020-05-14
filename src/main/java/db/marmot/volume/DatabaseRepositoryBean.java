package db.marmot.volume;

import db.marmot.repository.RepositoryFactoryBean;

/**
 * @author shaokang
 */
public class DatabaseRepositoryBean extends RepositoryFactoryBean<DataBaseRepository> {

	@Override
	public DataBaseRepository newInstance() {
		return new DataBaseRepository(getTemplates());
	}
	
}
