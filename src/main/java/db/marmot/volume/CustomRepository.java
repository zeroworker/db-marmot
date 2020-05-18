package db.marmot.volume;

import db.marmot.enums.TemplateType;
import db.marmot.graphic.FilterColumn;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class CustomRepository extends DataSourceRepository {
	
	public CustomRepository(Map<TemplateType, DataSourceTemplate> templates) {
		super(templates);
	}
	
	public List<Map<String, Object>> queryData(DataVolume dataVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		CustomTemplate customTemplate = getTemplate(TemplateType.custom);
		return customTemplate.queryData(dataVolume.getVolumeCode(), filterColumns, pageNum, pageSize);
	}
}
