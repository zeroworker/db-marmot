package db.marmot.statistical;

import db.marmot.repository.RepositoryFactoryBean;

/**
 * @author shaokang
 */
public class StatisticalRepositoryBean extends RepositoryFactoryBean<StatisticalRepository> {
	
	@Override
	public StatisticalRepository newInstance() {
		return new StatisticalRepository(getTemplates());
	}
	
}
