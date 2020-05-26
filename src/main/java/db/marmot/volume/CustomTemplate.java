package db.marmot.volume;

import db.marmot.graphic.FilterColumn;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public interface CustomTemplate {
	
	/**
	 * 获取源数据数据集字段
	 * @param volumeCode 数据集编码
	 * @return
	 */
	List<DataColumn> getMetadataColumns(String volumeCode);
	
	/**
	 * 查询明细数据
	 * @param volumeCode 数据集编码
	 * @param filterColumns 过滤条件
	 * @param pageNum 分页数
	 * @param pageSize 分页大小
	 * @return
	 */
	List<Map<String, Object>> queryData(String volumeCode, List<FilterColumn> filterColumns, int pageNum, int pageSize);
}
