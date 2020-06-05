package db.marmot.volume;

import db.marmot.enums.ColumnType;
import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.Validators;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * sql脚本 若相同字段不同用途 可以 amount as amount_1 amount as amount_2, amount as amount_3
	 */
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
	@NotNull
	@Size(min = 1)
	private List<DataColumn> dataColumns = new ArrayList<>();
	
	public void validateVolumeLimit(int validateLimit) {
		Validators.isTrue(validateLimit <= this.volumeLimit, "支持最大数据行为:%s", this.volumeLimit);
	}
	
	public void validateDataVolume(Database database) {
		Validators.assertJSR303(this);
		Validators.isTrue(volumeType != VolumeType.enums
				, "数据集不支持枚举");
		Validators.isTrue(volumeType != VolumeType.custom,
				() -> Validators.validateSqlSelect(database.getDbType()
						, this.sqlScript));
		Validators.isTrue(volumeType != VolumeType.custom,
				() -> Validators.notBlank(this.sqlScript
						, "sql不能为空"));
		Validators.isTrue(volumeType == VolumeType.model
				, () -> {
			findDateDataColumn();
			findIndexDataColumn();
		});
		dataColumns.forEach(dataColumn -> {
			dataColumn.validateDataColumn();
			Validators.notNull(findDataColumn(dataColumn.getScreenColumn(), null)
					, "筛选字段%s在数据集中不存在"
					, dataColumn.getScreenColumn());
		});
	}
	
	public DataColumn findDataColumn(String columnCode, ColumnType columnType) {
		DataColumn dataColumn = null;
		for (DataColumn column : dataColumns) {
			if (columnCode.equals(column.getColumnCode())) {
				dataColumn = column;
				break;
			}
		}
		Validators.notNull(dataColumn, "字段%s在数据集字段中不存在", columnCode);
		if (columnType != null) {
			Validators.isTrue(columnType == dataColumn.getColumnType(), "字段%s字段类型与数据集字段类型不匹配", columnCode);
		}
		return dataColumn;
	}
	
	public List<DataColumn> findFilterDataColumns() {
		return dataColumns.stream().filter(DataColumn::isColumnFilter).collect(Collectors.toList());
	}
	
	public DataColumn findIndexDataColumn() {
		Stream<DataColumn> stream = dataColumns.stream().filter(DataColumn::isColumnIndex);
		Validators.isTrue(stream.count() == 1, "数据集必须存在唯一的角标字段");
		return stream.findFirst().get();
	}
	
	public DataColumn findDateDataColumn() {
		Stream<DataColumn> stream = dataColumns.stream().filter(dataColumn -> dataColumn.getColumnType() == ColumnType.date);
		Validators.isTrue(stream.count() == 1, "数据集必须存在唯一的时间字段");
		return stream.findFirst().get();
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
