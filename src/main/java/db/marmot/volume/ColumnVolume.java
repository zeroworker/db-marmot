package db.marmot.volume;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.generator.ColumnEnum;
import db.marmot.volume.parser.SqlSelectQueryParser;

import lombok.Getter;
import lombok.Setter;

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
		if (dataColumns == null || dataColumns.isEmpty()){
			throw new ValidateException("字段数据集数据字段不能为空");
		}

		if (volumeType == VolumeType.sql) {
			//-解析一次sql,针对模型sql必须存在一定规范
			SqlSelectQueryParser sqlSelectQueryParser = new SqlSelectQueryParser(database.getDbType(), script).parse();
			if (sqlSelectQueryParser.getSelectTables().size() != 1) {
				throw new ValidateException("字段数据集sql必须为单表查询");
			}
		}
		
		if (volumeType == VolumeType.enums) {
			Class enumClass;
			try {
				enumClass = Class.forName(script);
			} catch (ClassNotFoundException e) {
				throw new ValidateException("枚举类不存在");
			}
			if (!enumClass.isEnum()) {
				throw new ValidateException("该类非枚举类型");
			}
			if (!ColumnEnum.class.isAssignableFrom(enumClass)) {
				throw new ValidateException("枚举必须实现 ColumnEnum");
			}
		}
	}
	
	/**
	 * 获取数据字段
	 * @param columnCode
	 * @return
	 */
	public DataColumn findDataColumn(String columnCode) {
		DataColumn dataColumn = null;
		for (DataColumn column : dataColumns) {
			if (columnCode.equals(column.getColumnCode())) {
				dataColumn = column;
				break;
			}
		}
		return dataColumn;
	}
}
