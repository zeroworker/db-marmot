package db.marmot.volume;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;

import db.marmot.enums.TemplateType;
import db.marmot.enums.VolumeType;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;

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
			throw new RepositoryException(String.format("数据集不能为空"));
		}
		
		setDataColumns(dataVolume);
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", dataVolume.getDbName()));
		}
		dataVolume.validateDataVolume(database);
		
		try {
			volumeTemplate.storeDataVolume(dataVolume);
		} catch (DuplicateKeyException keyException) {
			throw new RepositoryException("数据集重复");
		}
		
		try {
			volumeTemplate.storeDataColumn(dataVolume.getDataColumns());
		} catch (DuplicateKeyException keyException) {
			throw new RepositoryException(String.format("重复数据集字段"));
		}
	}
	
	private void setDataColumns(DataVolume dataVolume) {
		if (dataVolume.getVolumeType() == VolumeType.custom) {
			CustomTemplate customTemplate = getTemplate(TemplateType.custom);
			dataVolume.setDataColumns(customTemplate.getMetadataColumns(dataVolume));
		}
		if (dataVolume.getVolumeType() == VolumeType.model || dataVolume.getVolumeType() == VolumeType.sql) {
			dataVolume.setDataColumns(databaseTemplate.getDataColumns(dataVolume.getDbName(), dataVolume.getVolumeId(), dataVolume.getSqlScript()));
		}
	}
	
	/**
	 * 更新数据集 更新数据集 可能会造成仪表盘图表出现异常,该操作需要谨慎 避免出现字段减少的情况,可以增加字段
	 * @param dataVolume 数据集配置
	 */
	public void updateDataVolume(DataVolume dataVolume) {
		
		if (dataVolume == null) {
			throw new RepositoryException(String.format("数据集不能为空"));
		}
		
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", dataVolume.getDbName()));
		}
		
		DataVolume oldDataVolume = volumeTemplate.findDataVolume(dataVolume.getVolumeId());
		if (oldDataVolume == null) {
			throw new RepositoryException("数据集不存在,更新失败");
		}
		
		setDataColumns(dataVolume);
		dataVolume.validateDataVolume(database);
		
		oldDataVolume.getDataColumns().forEach(dataColumn -> {
			if (dataVolume.findDataColumn(dataColumn.getColumnCode()) == null) {
				throw new RepositoryException(String.format("更新数据集(不允许数据字段减少),原数据集字段%s不存在,更新失败"));
			}
		});
		
		volumeTemplate.updateDataVolume(dataVolume);
		volumeTemplate.deleteDataColumnByVolumeId(dataVolume.getVolumeId());
		
		try {
			volumeTemplate.storeDataColumn(dataVolume.getDataColumns());
		} catch (DuplicateKeyException keyException) {
			throw new RepositoryException(String.format("重复数据集字段"));
		}
	}
	
	/**
	 * 根据数据集ID删除数据
	 * @param volumeId 数据集ID
	 * @return
	 */
	public void deleteDataVolume(long volumeId) {
		
		DataVolume dataVolume = volumeTemplate.findDataVolume(volumeId);
		if (dataVolume == null) {
			throw new RepositoryException("数据集不存在,删除失败");
		}
		
		volumeTemplate.deleteDataVolume(volumeId);
		volumeTemplate.deleteDataColumnByVolumeId(volumeId);
	}
	
	/**
	 * 根据数据集ID获取数据集
	 * @param volumeId 数据集ID
	 * @return
	 */
	public DataVolume findDataVolume(long volumeId) {
		
		DataVolume dataVolume = volumeTemplate.findDataVolume(volumeId);
		if (dataVolume == null) {
			throw new RepositoryException(String.format("数据集不存在"));
		}
		dataVolume.setDataColumns(queryDataColumn(dataVolume.getVolumeId()));
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
			throw new RepositoryException(String.format("字段数据集不能为空"));
		}
		
		Database database = databaseTemplate.findDatabase(columnVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", columnVolume.getDbName()));
		}
		
		setDataColumns(columnVolume);
		columnVolume.validateColumnVolume(database);
		
		try {
			volumeTemplate.storeColumnVolume(columnVolume);
		} catch (DuplicateKeyException keyException) {
			throw new RepositoryException(String.format("重复字段数据集"));
		}
		
		if (columnVolume.getDataColumns() != null && !columnVolume.getDataColumns().isEmpty()) {
			try {
				volumeTemplate.storeDataColumn(columnVolume.getDataColumns());
			} catch (DuplicateKeyException keyException) {
				throw new RepositoryException(String.format("重复字段数据集字段%s", columnVolume.getColumnCode()));
			}
		}
	}
	
	private void setDataColumns(ColumnVolume columnVolume) {
		if (columnVolume.getVolumeType() == VolumeType.custom) {
			CustomTemplate customTemplate = getTemplate(TemplateType.custom);
			columnVolume.setDataColumns(customTemplate.getMetadataColumns(columnVolume));
		}
		if (columnVolume.getVolumeType() == VolumeType.sql) {
			columnVolume.setDataColumns(databaseTemplate.getDataColumns(columnVolume.getDbName(), columnVolume.getVolumeId(), columnVolume.getScript()));
		}
	}
	
	/**
	 * 更新字段集
	 * @param columnVolume 数据集配置
	 */
	public void updateColumnVolume(ColumnVolume columnVolume) {
		if (columnVolume == null) {
			throw new RepositoryException(String.format("字段数据集不能为空"));
		}
		
		if (volumeTemplate.findColumnVolume(columnVolume.getVolumeId()) == null) {
			throw new RepositoryException("数据集不存在,更新失败");
		}
		
		Database database = databaseTemplate.findDatabase(columnVolume.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%s不存在", columnVolume.getDbName()));
		}
		
		setDataColumns(columnVolume);
		columnVolume.validateColumnVolume(database);
		
		volumeTemplate.updateColumnVolume(columnVolume);
		
		if (columnVolume.getDataColumns() != null && !columnVolume.getDataColumns().isEmpty()) {
			volumeTemplate.deleteDataColumnByVolumeId(columnVolume.getVolumeId());
			try {
				volumeTemplate.storeDataColumn(columnVolume.getDataColumns());
			} catch (DuplicateKeyException keyException) {
				throw new RepositoryException(String.format("重复字段数据集字段"));
			}
		}
	}
	
	/**
	 * 根据数据集ID删除字段数据集
	 * @param volumeId 数据集ID
	 */
	public void deleteColumnVolume(long volumeId) {
		
		if (volumeTemplate.findColumnVolume(volumeId) == null) {
			throw new RepositoryException("字段数据集不存在,删除失败");
		}
		volumeTemplate.deleteColumnVolume(volumeId);
		volumeTemplate.deleteDataColumnByVolumeId(volumeId);
	}
	
	/**
	 * 根据数据集ID查询字段数据集
	 * @param volumeId 数据集ID
	 * @return
	 */
	public ColumnVolume findColumnVolume(long volumeId) {
		
		ColumnVolume columnVolume = volumeTemplate.findColumnVolume(volumeId);
		if (columnVolume == null) {
			throw new RepositoryException(String.format("字段数据集不存在"));
		}
		return columnVolume;
	}
	
	/**
	 * 根据字段编码查询字段数据集
	 * @param columnCode 字段编码
	 * @return
	 */
	public ColumnVolume findColumnVolume(String columnCode) {
		
		ColumnVolume columnVolume = volumeTemplate.findColumnVolume(columnCode);
		if (columnVolume == null) {
			throw new RepositoryException(String.format("字段数据集不存在"));
		}
		columnVolume.setDataColumns(queryDataColumn(columnVolume.getVolumeId()));
		return columnVolume;
	}
	
	/**
	 * 查询字段数据集
	 * @param columnCode 字段编码
	 * @param volumeType 数据集类型
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return
	 */
	public List<ColumnVolume> queryPageColumnVolume(String columnCode, VolumeType volumeType, int pageNum, int pageSize) {
		return volumeTemplate.queryPageColumnVolume(columnCode, volumeType != null ? volumeType.getCode() : null, pageNum, pageSize);
	}
	
	/**
	 * 保存数据集字段
	 * @param dataColumn 数据集配置
	 */
	public void storeDataColumn(DataColumn dataColumn) {
		
		if (dataColumn == null) {
			throw new RepositoryException("数据字段不能为空");
		}
		
		dataColumn.validateDataColumn();
		
		DataVolume dataVolume = findDataVolume(dataColumn.getVolumeId());
		if (!dataVolume.addDataColumn(dataColumn)) {
			throw new RepositoryException(String.format("重复数据集字段 %s", dataColumn.getColumnCode()));
		}
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException("数据源不存在");
		}
		dataVolume.validateDataVolume(database);
		
		volumeTemplate.storeDataColumn(dataColumn);
	}
	
	/**
	 * 更新数据集字段
	 * @param dataColumn 数据集配置
	 */
	public void updateDataColumn(DataColumn dataColumn) {
		
		if (dataColumn == null) {
			throw new RepositoryException(String.format("数据字段不能为空"));
		}
		
		dataColumn.validateDataColumn();
		
		DataVolume dataVolume = findDataVolume(dataColumn.getVolumeId());
		if (!dataVolume.updateDataColumn(dataColumn)) {
			throw new RepositoryException("数据字段不存在,更新失败");
		}
		
		Database database = databaseTemplate.findDatabase(dataVolume.getDbName());
		if (database == null) {
			throw new RepositoryException("数据源不存在");
		}
		
		dataVolume.validateDataVolume(database);
		
		volumeTemplate.updateDataColumn(dataColumn);
	}
	
	/**
	 * 根据字段ID查询数据集字段
	 * @param columnId 字段ID
	 * @return
	 */
	public DataColumn findDataColumn(long columnId) {
		
		DataColumn dataColumn = volumeTemplate.findDataColumn(columnId);
		if (dataColumn == null) {
			throw new RepositoryException(String.format("数据字段不存在"));
		}
		return dataColumn;
	}
	
	/**
	 * 根据数据集ID以及字段编码查询数据集字段
	 * @param volumeId 字段ID
	 * @param columnCode 字段ID
	 * @return
	 */
	public DataColumn findDataColumn(long volumeId, String columnCode) {
		
		DataColumn dataColumn = volumeTemplate.findDataColumn(volumeId, columnCode);
		if (dataColumn == null) {
			throw new RepositoryException(String.format("数据字段不存在"));
		}
		return dataColumn;
	}
	
	/**
	 * 根据数据集ID查询数据集字段 若数据ID无数据字段 返回空
	 * @param volumeId 数据集ID
	 * @return
	 */
	public List<DataColumn> queryDataColumn(long volumeId) {
		
		List<DataColumn> dataColumns = volumeTemplate.queryDataColumn(volumeId);
		if (dataColumns != null && !dataColumns.isEmpty()) {
			return volumeTemplate.queryDataColumn(volumeId);
		}
		throw new RepositoryException(String.format("数据集数据字段不存在"));
	}
	
	/**
	 * 根据字段ID删除数据字段
	 * @param columnId 数据集ID
	 * @return
	 */
	public void deleteDataColumnByColumnId(long columnId) {
		
		if (volumeTemplate.findDataColumn(columnId) == null) {
			throw new RepositoryException("数据字段不存在,删除失败");
		}
		volumeTemplate.deleteDataColumnByColumnId(columnId);
	}
}
