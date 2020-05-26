package db.marmot.volume;

import com.alibaba.druid.util.JdbcUtils;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;

/**
 * @author shaokang
 */
@Setter
@Getter
public class Database {
	
	/**
	 * 自增序列
	 */
	private long id;
	
	/**
	 * 数据库名称
	 */
	@NotBlank
	private String name = "master";
	
	/**
	 * 数据库类型
	 */
	@NotNull
	private String dbType;
	
	/**
	 * 数据库地址
	 */
	@NotBlank
	private String url;
	
	/**
	 * 数据库登陆名
	 */
	@NotBlank
	private String userName = "unknown";
	
	/**
	 * 数据库密码
	 */
	@NotBlank
	private String password = "unknown";
	
	public Database() {
	}
	
	public Database(DataSource dataSource) {
		try {
			this.url = dataSource.getConnection().getMetaData().getURL();
			this.dbType = JdbcUtils.getDbType(this.url, null);
		} catch (SQLException e) {
			new ValidateException("获取数据源链接异常", e);
		}
	}
	
	public void validateDatabase() {
		Validators.assertJSR303(this);
	}
}
