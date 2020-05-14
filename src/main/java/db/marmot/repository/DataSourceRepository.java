package db.marmot.repository;

import java.util.Map;

import db.marmot.enums.TemplateType;
import db.marmot.repository.validate.Validators;

/**
 * @author shaokang
 */
public abstract class DataSourceRepository {

	private Map<TemplateType, DataSourceTemplate> templates;

	public DataSourceRepository(Map<TemplateType, DataSourceTemplate> templates) {
		Validators.notNull(templates, "templates不能为空");
		this.templates = templates;
	}

	public <T extends DataSourceTemplate> T getTemplate(TemplateType templateType) {
		DataSourceTemplate dataSourceTemplate = templates.get(templateType);
		if (dataSourceTemplate == null) {
			throw new RepositoryException(String.format("仓储%s template未定义", templateType.getCode()));
		}
		return (T) dataSourceTemplate;
	}
}
