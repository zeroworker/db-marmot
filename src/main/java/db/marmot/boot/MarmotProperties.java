package db.marmot.boot;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import db.marmot.repository.validate.Validators;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "db.marmot")
public class MarmotProperties implements InitializingBean {
	
	/**
	 * 启用/关闭
	 */
	private boolean enable = true;
	
	/**
	 * 是否分表
	 */
	private boolean sharding = false;
	
	/**
	 * 模型数-统计模型线程最大线程数
	 */
	private int modelThreadSize = 50;

	/**
	 * 模型延时订正时间-单位分钟
	 */
	private int modelReviseDelay = 60;

	/**
	 * 文件路径
	 */
	private String fileUrl;
	
	/**
	 * 文件下载线程数
	 */
	private int downloadThreadSize = 5;
	
	/**
	 * 下载文件地址
	 */
	private String downloadUrl;
	
	@Override
	public void afterPropertiesSet() {
		Validators.notBlank(this.fileUrl, "fileUrl 不能为空");
		Validators.notBlank(this.downloadUrl, "downloadUrl 不能为空");
		Validators.isTrue(this.modelThreadSize > 0, "modelThreadSize 必须大于零");
		Validators.isTrue(this.downloadThreadSize > 0, "downloadThreadSize 必须大于零");
	}
}
