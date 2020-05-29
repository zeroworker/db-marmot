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
	
	public List<Map<String, Object>> queryCustomData(String volumeCode, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		CustomTemplate customTemplate = dataSourceTemplate.getCustomTemplate();
		return customTemplate.queryData(volumeCode, filterColumns, pageNum, pageSize);
	}
}
