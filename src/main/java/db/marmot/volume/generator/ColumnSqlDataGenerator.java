package db.marmot.volume.generator;

import java.util.List;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.RepositoryType;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.volume.DataBaseRepository;
import db.marmot.volume.DataColumn;
import db.marmot.volume.Database;

/**
 * @author shaokang
 */
public class ColumnSqlDataGenerator extends AbstractColumnDataGenerator {
	
	private RepositoryAdapter repositoryAdapter;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public ColumnSqlDataGenerator(RepositoryAdapter repositoryAdapter) {
		this.repositoryAdapter = repositoryAdapter;
	}
	
	@Override
	protected void generateData(ColumnData columnData, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		DataBaseRepository baseRepository = repositoryAdapter.getRepository(RepositoryType.database);
		Database database = baseRepository.findDatabase(columnData.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), columnData.getScript());
		if (filterColumns != null && !filterColumns.isEmpty()) {
			for (FilterColumn filterColumn : filterColumns) {
				DataColumn dataColumn = columnData.findDataColumn(filterColumn.getColumnCode());
				if (dataColumn == null) {
					throw new GraphicGeneratorException(String.format("过滤字段%s在字段数据集字段集合中不存在", filterColumn.getColumnCode()));
				}
				if (filterColumn.getColumnType() != dataColumn.getColumnType()) {
					throw new GraphicGeneratorException(String.format("过滤字段类型与数据集字段类型不匹配", filterColumn.getColumnCode()));
				}
				sqlBuilder.addCondition(filterColumn.getOperators(), filterColumn.getColumnType(), dataColumn.getScreenColumn(), filterColumn.getRightValue());
			}
		}
		columnData.setScript(sqlBuilder.addLimit(pageNum, pageSize).toSql());
		columnData.setData(baseRepository.queryData(columnData.getDbName(), columnData.getScript()));
	}
	
	@Override
	public VolumeType volumeType() {
		return VolumeType.sql;
	}
	
}
