package db.marmot.volume;

import java.util.List;
import java.util.Map;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.DataSourceTemplate;

/**
 * @author shaokang
 */
public interface CustomTemplate extends DataSourceTemplate {
	
	/**
	 * 获取源数据数据集字段
	 * @param columnVolume 字段数据集
	 * @return
	 */
	List<DataColumn> getMetadataColumns(ColumnVolume columnVolume);

	/**
	 * 获取源数据数据集字段
	 * @param dataVolume 数据集
	 * @return
	 */
	List<DataColumn> getMetadataColumns(DataVolume dataVolume);

	/**
	 * 查询明细数据
	 * @param dataVolume 数据集
	 * @param filterColumns 过滤条件
	 * @param pageNum 分页数
	 * @param pageSize 分页大小
	 * @return
	 */
	List<Map<String, Object>> queryData(DataVolume dataVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize);
}
