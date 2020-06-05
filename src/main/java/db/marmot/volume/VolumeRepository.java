package db.marmot.volume;

import db.marmot.enums.VolumeType;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

/**
 * @author shaokang
 */
public class VolumeRepository extends StatisticalRepository {
	
	public VolumeRepository(DataSourceTemplate dataSourceTemplate) {
		super(dataSourceTemplate);
	}
	
	/**
	 * 保存数据集
	 * @param dataVolume 数据集配置
	 */
	public void storeDataVolume(DataVolume dataVolume) {
		Validators.notNull(dataVolume, "数据集不能为空");
		Database database = dataSourceTemplate.findDatabase(dataVolume.getDbName());
		Validators.notNull(dataVolume, String.format("数据源%s不存在", dataVolume.getDbName()));
		try {
			dataSourceTemplate.storeDataVolume(dataVolume);
		} catch (DuplicateKeyException keyException) {
			dataSourceTemplate.updateDataVolume(dataVolume);
			dataSourceTemplate.deleteDataColumnByVolumeCode(dataVolume.getVolumeCode());
		} finally {
			if (dataVolume.getVolumeType() == VolumeType.custom) {
				CustomTemplate customTemplate = dataSourceTemplate.getCustomTemplate();
				List<DataColumn> dataColumns = customTemplate.getMetadataColumns(dataVolume.getVolumeCode());
				Validators.notEmpty(dataColumns, "数据集字段为空");
				dataVolume.setDataColumns(dataColumns);
			}
			if (dataVolume.getVolumeType() == VolumeType.model || dataVolume.getVolumeType() == VolumeType.sql) {
				List<DataColumn> dataColumns = dataSourceTemplate.getDataColumns(dataVolume.getDbName(), dataVolume.getVolumeCode(), dataVolume.getSqlScript());
				Validators.notEmpty(dataColumns, "数据集字段为空");
				if (CollectionUtils.isNotEmpty(dataVolume.getDataColumns())) {
					for (int i = 0; i < dataColumns.size(); i++) {
						DataColumn dataColumn = dataVolume.findDataColumn(dataColumns.get(i).getColumnCode(), null);
						if (dataColumn != null) {
							dataColumn.setColumnOrder(dataColumns.get(i).getColumnOrder());
							dataColumn.setColumnType(dataColumns.get(i).getColumnType());
							dataColumn.addColumnName(dataColumns.get(i).getColumnName());
							dataColumn.addScreenColumn(dataColumns.get(i).getScreenColumn());
							dataColumn.addContent(dataColumns.get(i).getContent());
							dataColumns.add(i, dataColumn);
						}
					}
				}
				dataVolume.setDataColumns(dataColumns);
			}
			dataVolume.validateDataVolume(database);
			dataSourceTemplate.storeDataColumn(dataVolume.getDataColumns());
		}
	}
	
	/**
	 * 根据数据集编码获取数据集
	 * @param volumeCode 数据集编码
	 * @return
	 */
	public DataVolume findDataVolume(String volumeCode) {
		DataVolume dataVolume = dataSourceTemplate.findDataVolume(volumeCode);
		Validators.notNull(dataVolume, "数据集存在");
		List<DataColumn> dataColumns = dataSourceTemplate.queryDataColumn(dataVolume.getVolumeCode());
		Validators.isTrue(CollectionUtils.isNotEmpty(dataColumns), String.format("数据集%s数据字段不存在", dataVolume.getVolumeCode()));
		dataVolume.setDataColumns(dataColumns);
		return dataVolume;
	}
	
	/**
	 * 根据数据集名称查询数据集 若name 为空 默认查询所有;支持模糊查询
	 * @param volumeName 数据集名称
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return
	 */
	public List<DataVolume> queryPageDataVolume(String volumeName, int pageNum, int pageSize) {
		return dataSourceTemplate.queryPageDataVolume(volumeName, pageNum, pageSize);
	}
	
	/**
	 * 保存字段集
	 * @param columnVolume 数据集配置
	 */
	public void storeColumnVolume(ColumnVolume columnVolume) {
		Validators.notNull(columnVolume, "字段数据集不能为空");
		Database database = dataSourceTemplate.findDatabase(columnVolume.getDbName());
		Validators.notNull(database, String.format("数据源%s不存在", columnVolume.getDbName()));
		try {
			dataSourceTemplate.storeColumnVolume(columnVolume);
		} catch (DuplicateKeyException keyException) {
			dataSourceTemplate.updateColumnVolume(columnVolume);
			dataSourceTemplate.deleteDataColumnByVolumeCode(columnVolume.getVolumeCode());
		} finally {
			if (columnVolume.getVolumeType() == VolumeType.custom) {
				CustomTemplate customTemplate = dataSourceTemplate.getCustomTemplate();
				columnVolume.setDataColumns(customTemplate.getMetadataColumns(columnVolume.getVolumeCode()));
				dataSourceTemplate.storeDataColumn(columnVolume.getDataColumns());
			}
			if (columnVolume.getVolumeType() == VolumeType.sql) {
				columnVolume.setDataColumns(dataSourceTemplate.getDataColumns(columnVolume.getDbName(), columnVolume.getVolumeCode(), columnVolume.getScript()));
				dataSourceTemplate.storeDataColumn(columnVolume.getDataColumns());
			}
			columnVolume.validateColumnVolume(database);
		}
	}
	
	/**
	 * 根据字段编码查询字段数据集
	 * @param columnCode 字段编码
	 * @return
	 */
	public ColumnVolume findColumnVolume(String columnCode) {
		ColumnVolume columnVolume = dataSourceTemplate.findColumnVolume(columnCode);
		Validators.notNull(columnVolume, "字段数据集不存在");
		List<DataColumn> dataColumns = dataSourceTemplate.queryDataColumn(columnVolume.getVolumeCode());
		Validators.isTrue(CollectionUtils.isNotEmpty(dataColumns), String.format("数据集%s数据字段不存在", columnVolume.getVolumeCode()));
		columnVolume.setDataColumns(dataColumns);
		return columnVolume;
	}
	
	/**
	 * 根据数据集编码以及字段编码查询数据集字段
	 * @param volumeCode 数据集编码
	 * @param columnCode 字段编码
	 * @return
	 */
	public DataColumn findDataColumn(String volumeCode, String columnCode) {
		DataColumn dataColumn = dataSourceTemplate.findDataColumn(volumeCode, columnCode);
		Validators.notNull(dataColumn, "数据集字段不存在");
		return dataColumn;
	}
}
