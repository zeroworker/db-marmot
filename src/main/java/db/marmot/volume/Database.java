package db.marmot.volume;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.alibaba.druid.util.JdbcUtils;
import db.marmot.repository.validate.Validators;

import lombok.Getter;
import lombok.Setter;

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
	private String name;

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
	private String userName;
	
	/**
	 * 数据库密码
	 */
	@NotBlank
	private String password;

	public void setUrl(String url) {
		this.url = url;
		this.dbType = JdbcUtils.getDbType(url,null);
	}

	public void validateDatabase() {
		Validators.assertJSR303(this);
	}
}
