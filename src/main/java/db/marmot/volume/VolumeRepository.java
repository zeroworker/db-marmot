package db.marmot.volume;

import db.marmot.enums.TemplateType;
import db.marmot.enums.VolumeType;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class VolumeRepository extends DataSourceRepository {
	
	private VolumeTemplate volumeTemplate;
	private DatabaseTemplate databaseTemplate;
	
	public VolumeRepository(Map<TemplateType, DataSourceTemplate> templates) {
		super(templates);
		this.volumeTemplate = getTemplate(TemplateType.volume);
		this.databaseTemplate = getTemplate(TemplateType.database);
	}
	
	/**
	 * 保存数据集
	 * @param dataVolume 数据集配置
	 */
	public void storeDataVolume(DataVolume dataVolume) {
		if (dataVolume == null) {
			throw new RepositoryException("数据集不能为空");
		}
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", dataVolume.getDbName()));
		}
		try {
			volumeTemplate.storeDataVolume(dataVolume);
		} catch (DuplicateKeyException keyException) {
			volumeTemplate.updateDataVolume(dataVolume);
			volumeTemplate.deleteDataColumnByVolumeCode(dataVolume.getVolumeCode());
		} finally {
			if (dataVolume.getVolumeType() == VolumeType.custom) {
				CustomTemplate customTemplate = getTemplate(TemplateType.custom);
				dataVolume.setDataColumns(customTemplate.getMetadataColumns(dataVolume.getVolumeCode()));
			}
			if (dataVolume.getVolumeType() == VolumeType.model || dataVolume.getVolumeType() == VolumeType.sql) {
				dataVolume.setDataColumns(databaseTemplate.getDataColumns(dataVolume.getDbName(), dataVolume.getVolumeCode(), dataVolume.getSqlScript()));
			}
			dataVolume.validateDataVolume(database);
			volumeTemplate.storeDataColumn(dataVolume.getDataColumns());
		}
	}
	
	/**
	 * 根据数据集编码获取数据集
	 * @param volumeCode 数据集编码
	 * @return
	 */
	public DataVolume findDataVolume(String volumeCode) {
		DataVolume dataVolume = volumeTemplate.findDataVolume(volumeCode);
		if (dataVolume == null) {
			throw new RepositoryException("数据集不存在");
		}
		List<DataColumn> dataColumns = volumeTemplate.queryDataColumn(dataVolume.getVolumeCode());
		if (dataColumns == null || dataColumns.isEmpty()) {
			throw new RepositoryException(String.format("数据集%s数据字段不存在", dataVolume.getVolumeCode()));
		}
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
		return volumeTemplate.queryPageDataVolume(volumeName, pageNum, pageSize);
	}
	
	/**
	 * 保存字段集
	 * @param columnVolume 数据集配置
	 */
	public void storeColumnVolume(ColumnVolume columnVolume) {
		if (columnVolume == null) {
			throw new RepositoryException("字段数据集不能为空");
		}
		Database database = databaseTemplate.findDatabase(columnVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", columnVolume.getDbName()));
		}
		try {
			volumeTemplate.storeColumnVolume(columnVolume);
		} catch (DuplicateKeyException keyException) {
			volumeTemplate.updateColumnVolume(columnVolume);
			volumeTemplate.deleteDataColumnByVolumeCode(columnVolume.getVolumeCode());
		} finally {
			if (columnVolume.getVolumeType() == VolumeType.custom) {
				CustomTemplate customTemplate = getTemplate(TemplateType.custom);
				columnVolume.setDataColumns(customTemplate.getMetadataColumns(columnVolume.getVolumeCode()));
				volumeTemplate.storeDataColumn(columnVolume.getDataColumns());
			}
			if (columnVolume.getVolumeType() == VolumeType.sql) {
				columnVolume.setDataColumns(databaseTemplate.getDataColumns(columnVolume.getDbName(), columnVolume.getVolumeCode(), columnVolume.getScript()));
				volumeTemplate.storeDataColumn(columnVolume.getDataColumns());
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
		ColumnVolume columnVolume = volumeTemplate.findColumnVolume(columnCode);
		if (columnVolume == null) {
			throw new RepositoryException(String.format("字段数据集不存在,字段编码:%s", columnCode));
		}
		List<DataColumn> dataColumns = volumeTemplate.queryDataColumn(columnVolume.getVolumeCode());
		if (dataColumns == null || dataColumns.isEmpty()) {
			throw new RepositoryException(String.format("数据集%s数据字段不存在", columnVolume.getVolumeCode()));
		}
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
		DataColumn dataColumn = volumeTemplate.findDataColumn(volumeCode, columnCode);
		if (dataColumn == null) {
			throw new RepositoryException("数据字段不存在");
		}
		return dataColumn;
	}
	
}
