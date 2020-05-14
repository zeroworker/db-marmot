package db.marmot.repository;

import db.marmot.enums.RepositoryType;

/**
 * @author shaokang
 */
public interface RepositoryAdapter {

	void setSharding(boolean sharding);

	/**
	 * 根据仓库类型获取仓储服务
	 * @param repository
	 * @param <R>
	 * @return
	 */
	<R extends DataSourceRepository> R getRepository(RepositoryType repository);
}
