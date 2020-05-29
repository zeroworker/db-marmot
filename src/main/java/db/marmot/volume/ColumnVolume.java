package db.marmot.volume;

import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.generator.ColumnEnum;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 字段数据集
 * @author shaokang
 */
@Setter
@Getter
public class ColumnVolume {
	
	/**
	 * ID
	 */
	private long volumeId;
	
	/**
	 * 数据集名称
	 */
	@NotBlank
	@Size(max = 1024)
	@Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9_.()（）]+$", message = "数据集名称只能由中文、英文、数字及和\"_.()（）构成\"")
	private String volumeName;
	
	/**
	 * 数据集编码
	 */
	@NotBlank
	@Size(max = 512)
	private String volumeCode;
	
	/**
	 * 类型
	 */
	@NotNull
	private VolumeType volumeType;
	
	/**
	 * 数据源名称
	 */
	@NotBlank
	private String dbName;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	@Size(max = 512)
	@Pattern(regexp = "^[a-zA-Z_]+$", message = "必须是英文字母支持大小写以及下划线")
	private String columnCode;
	
	/**
	 * 字段值编码
	 */
	@NotBlank
	@Size(max = 512)
	private String columnValueCode;
	
	/**
	 * 字段展示编码
	 */
	@NotBlank
	@Size(max = 512)
	private String columnShowCode;
	
	/**
	 * 脚本-sql/枚举类路径
	 */
	@NotBlank
	private String script;
	
	/**
	 * 描述
	 */
	@Size(max = 512)
	private String content;
	
	/**
	 * 数据字段
	 */
	private List<DataColumn> dataColumns = new ArrayList<>();
	
	public void validateColumnVolume(Database database) {
		Validators.assertJSR303(this);
		if (volumeType == VolumeType.model) {
			throw new ValidateException("字段数据集不支持模型");
		}
		if (dataColumns == null || dataColumns.isEmpty()) {
			throw new ValidateException("字段数据集数据字段不能为空");
		}
		if (volumeType == VolumeType.sql) {
			Validators.validateSqlSelect(database.getDbType(), this.script);
		}
		if (volumeType == VolumeType.enums) {
			try {
				Class enumClass = Class.forName(script);
				if (!enumClass.isEnum() || !ColumnEnum.class.isAssignableFrom(enumClass)) {
					throw new ValidateException("class 必须是枚举并且必须实现 ColumnEnum");
				}
			} catch (ClassNotFoundException e) {
				throw new ValidateException("枚举类不存在");
			}
		}
	}
}
