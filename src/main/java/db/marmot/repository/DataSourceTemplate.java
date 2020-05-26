package db.marmot.repository;

import db.marmot.graphic.GraphicTemplate;
import db.marmot.repository.validate.ValidateException;
import db.marmot.volume.CustomTemplate;

import javax.sql.DataSource;

/**
 * @author shaokang
 */
public class DataSourceTemplate extends GraphicTemplate {
	
	private CustomTemplate customTemplate;
	
	public DataSourceTemplate(DataSource dataSource, CustomTemplate customTemplate) {
		super(dataSource);
		this.customTemplate = customTemplate;
	}
	
	public CustomTemplate getCustomTemplate() {
		if (customTemplate == null) {
			throw new ValidateException("customTemplate未实现");
		}
		return customTemplate;
	}
}
