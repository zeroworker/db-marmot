package db.marmot.volume.generator;

import java.util.List;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.RepositoryAdapter;

/**
 * @author shaokang
 */
public interface ColumnGeneratorAdapter {
	
	void setRepositoryAdapter(RepositoryAdapter repositoryAdapter);
	
	/**
	 * 生成字段数据集数据 枚举不做分页,sql 分页处理
	 * @param volumeId 数据集ID
	 * @param columnCode 字段编码
	 * @param filterColumns 过滤字段
	 * @param pageNum 分页数
	 * @param pageSize 每页大小
	 * @return
	 */
	ColumnData generateColumnData(long volumeId, String columnCode, List<FilterColumn> filterColumns, int pageNum, int pageSize);
	
}
