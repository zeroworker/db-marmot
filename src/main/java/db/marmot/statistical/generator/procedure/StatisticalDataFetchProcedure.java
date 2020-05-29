package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.Operators;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTask;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataRange;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataFetchProcedure implements StatisticalProcedure {
	
	private DataSourceRepository dataSourceRepository;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public StatisticalDataFetchProcedure(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	public boolean match(DataVolume dataVolume, StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		DataColumn indexDataColumn = dataVolume.findIndexDataColumn();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		sqlBuilder.addSelectAggregateItem(Aggregates.min, indexDataColumn.getColumnCode(), "minValue").addSelectAggregateItem(Aggregates.max, indexDataColumn.getColumnCode(), "maxValue");
		
		temporaryMemory.addThisTask(dataSourceRepository.findStatisticalTask(statisticalModel.getModelName()));
		if (temporaryMemory.hashThisTask()) {
			sqlBuilder.addCondition(Operators.greater_than, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), temporaryMemory.getThisTask().getEndIndex());
		}
		
		DataRange dataRange = dataSourceRepository.getDataRange(database.getName(), sqlBuilder.toSql());
		if (dataRange != null) {
			temporaryMemory.addNextTask(dataRange,dataVolume, statisticalModel);
		}
		
		return temporaryMemory.hashThisTask() ? true : false;
	}
	
	@Override
	public void processed(DataVolume dataVolume, StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		DataColumn indexDataColumn = dataVolume.findIndexDataColumn();
		StatisticalTask statisticalTask = temporaryMemory.getThisTask();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		sqlBuilder.addCondition(Operators.greater_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), statisticalTask.getStartIndex());
		sqlBuilder.addCondition(Operators.less_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), statisticalTask.getEndIndex());
		temporaryMemory.addMetaData(dataSourceRepository.querySourceData(database.getName(), sqlBuilder.toSql()));
	}
	
	@Override
	public int getOrder() {
		return 1;
	}
}
