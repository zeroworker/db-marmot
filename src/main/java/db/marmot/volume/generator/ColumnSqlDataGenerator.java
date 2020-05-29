package db.marmot.volume.generator;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.ColumnVolume;
import db.marmot.volume.DataColumn;
import db.marmot.volume.Database;

import java.util.List;

/**
 * @author shaokang
 */
public class ColumnSqlDataGenerator extends AbstractColumnDataGenerator {
	
	private DataSourceRepository dataSourceRepository;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public ColumnSqlDataGenerator(DataSourceRepository dataSourceRepository) {
		Validators.notNull(dataSourceRepository, "dataSourceRepository 不能为空");
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	protected ColumnData generateData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		ColumnData columnData = new ColumnData(columnVolume.getScript(), columnVolume.getColumnValueCode(), columnVolume.getColumnShowCode());
		Database database = dataSourceRepository.findDatabase(columnVolume.getDbName());
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
		columnData.setData(dataSourceRepository.querySourceData(columnVolume.getDbName(), columnData.getScript()));
		return columnData;
	}
	
	@Override
	public VolumeType volumeType() {
		return VolumeType.sql;
	}
	
}
