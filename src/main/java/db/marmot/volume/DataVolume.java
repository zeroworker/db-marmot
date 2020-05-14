package db.marmot.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.ColumnType;
import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.parser.SelectColumn;
import db.marmot.volume.parser.SqlSelectQueryParser;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据集
 * @author shaokang
 */
@Setter
@Getter
public class DataVolume {
	
	/**
	 * 数据集ID
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
	 * 数据集类型
	 */
	@NotNull
	private VolumeType volumeType;

	/**
	 * 数据源名称
	 */
	@NotBlank
	private String dbName;

	/**
	 * 数据源类型
	 */
	@NotBlank
	private String dbType;
	
	/**
	 * sql脚本 若相同字段不同用途 可以 amount as amount_1 amount as amount_2, amount as amount_3
	 */
	@NotBlank
	private String sqlScript;
	
	/**
	 * 数据集数据量
	 */
	private long volumeLimit = 10000;
	
	/**
	 * 描述
	 */
	@Size(max = 512)
	private String content;
	
	/**
	 * 数据字段
	 */
	@Valid
	@NotNull
	@Size(min = 1)
	private List<DataColumn> dataColumns = new ArrayList<>();

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
	
	/**
	 * 获取时间数据字段
	 * @return
	 */
	public DataColumn findDateDataColumn() {
		DataColumn dataColumn = null;
		for (DataColumn column : dataColumns) {
			if (column.getColumnType() == ColumnType.date) {
				dataColumn = column;
				break;
			}
		}
		return dataColumn;
	}
	
	/**
	 * 验证数据集字段
	 */
	public void validateDataVolume(Database database) {

		Validators.assertJSR303(this);

		//-数据集不支持枚举
		if (volumeType == VolumeType.enums) {
			throw new ValidateException(String.format("数据集不支持枚举"));
		}

		if (volumeType == VolumeType.sql || volumeType == VolumeType.model){
			new SqlSelectQueryParser(database.getDbType(), sqlScript).parse();
		}

		//-模型数据集必须存在唯一时间字段
		if (volumeType == VolumeType.model) {
			int dateColumnNum = 0;
			for (DataColumn dataColumn : dataColumns) {
				if (dataColumn.getColumnType() == ColumnType.date) {
					dateColumnNum++;
				}
			}
			if (dateColumnNum != 1) {
				throw new ValidateException("数据集必须存在时间字段并且只能存在唯一时间字段");
			}
		}
	}
	
	/**
	 * 添加数据字段
	 * @param dataColumn
	 * @return
	 */
	public boolean addDataColumn(DataColumn dataColumn) {
		return dataColumns.add(dataColumn);
	}
	
	/**
	 * 更新数据字段
	 * @param dataColumn
	 */
	public boolean updateDataColumn(DataColumn dataColumn) {
		int index = dataColumns.indexOf(dataColumn);
		if (index != -1) {
			dataColumns.remove(index);
			return dataColumns.add(dataColumn);
		}
		return false;
	}
	
	/**
	 * 添加时间字段
	 * @param statisticalModel
	 * @param selectColumn
	 */
	public void addTimeColumn(StatisticalModel statisticalModel, SelectColumn selectColumn) {
		DataColumn dataColumn = findDateDataColumn();
		if (dataColumn.getColumnCode().equals(selectColumn.getColumnAlias())) {
			statisticalModel.setTimeColumn(dataColumn.getColumnCode());
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		DataVolume that = (DataVolume) o;
		return Objects.equals(volumeName, that.volumeName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(volumeName);
	}
}
