package db.marmot.volume;

import db.marmot.repository.RepositoryFactoryBean;

/**
 * @author shaokang
 */
public class VolumeRepositoryBean extends RepositoryFactoryBean<VolumeRepository> {
	@Override
	public VolumeRepository newInstance() {
		return new VolumeRepository(getTemplates());
	}

}
