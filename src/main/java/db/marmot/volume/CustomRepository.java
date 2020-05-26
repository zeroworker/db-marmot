package db.marmot.volume;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.DataSourceTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class CustomRepository extends DataBaseRepository {
	
	public CustomRepository(DataSourceTemplate dataSourceTemplate) {
		super(dataSourceTemplate);
	}
	
	/**
	 * 获取数据
	 * @param dataVolume
	 * @param filterColumns
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<Map<String, Object>> queryCustomData(DataVolume dataVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		CustomTemplate customTemplate = dataSourceTemplate.getCustomTemplate();
		return customTemplate.queryData(dataVolume.getVolumeCode(), filterColumns, pageNum, pageSize);
	}
}
