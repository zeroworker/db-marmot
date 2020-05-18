package db.marmot.volume.generator;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.RepositoryAdapter;

import java.util.List;

/**
 * @author shaokang
 */
public interface ColumnGeneratorAdapter {
	
	void setRepositoryAdapter(RepositoryAdapter repositoryAdapter);
	
	/**
	 * 生成字段数据集数据 枚举不做分页,sql 分页处理
	 * @param volumeCode 数据集编码
	 * @param columnCode 字段编码
	 * @param filterColumns 过滤字段
	 * @param pageNum 分页数
	 * @param pageSize 每页大小
	 * @return
	 */
	ColumnData generateColumnData(String volumeCode, String columnCode, List<FilterColumn> filterColumns, int pageNum, int pageSize);
	
}
