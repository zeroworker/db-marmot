package db.marmot.repository;

import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.graphic.GraphicTemplate;
import db.marmot.repository.validate.ValidateException;
import db.marmot.volume.CustomTemplate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author shaokang
 */
public class DataSourceTemplate extends GraphicTemplate {
	
	private CustomTemplate customTemplate;
	
	public DataSourceTemplate(DataSource dataSource, CustomTemplate customTemplate) {
		super(dataSource);
		this.customTemplate = customTemplate;
	}
	
	/**
	 * 获取自定义Template
	 * @return
	 */
	public CustomTemplate getCustomTemplate() {
		if (customTemplate == null) {
			throw new ValidateException("customTemplate未实现");
		}
		return customTemplate;
	}
	
	/**
	 * 获取数据库时间
	 * @return
	 */
	public Date getDataSourceTime() {
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType, null).dataSourceTime();
		return DataAccessUtils.requiredSingleResult(jdbcTemplate.query(sqlBuilder.toSql(), new RowMapper<Date>() {
			@Override
			public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getDate(1);
			}
		}));
	}
}
