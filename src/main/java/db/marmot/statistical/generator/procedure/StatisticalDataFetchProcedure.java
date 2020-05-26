package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTask;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.DataRange;
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
	public boolean match(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		Database database = dataSourceRepository.findDatabase(statisticalModel.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), statisticalModel.getFetchSql());
		sqlBuilder.addSelectAggregateItem(Aggregates.min, statisticalModel.getIndexColumn(), "minValue").addSelectAggregateItem(Aggregates.max, statisticalModel.getIndexColumn(), "maxValue");
		
		temporaryMemory.addThisTask(dataSourceRepository.findStatisticalTask(statisticalModel.getModelName()));
		if (temporaryMemory.hashThisTask()) {
			sqlBuilder.addCondition(Operators.greater_than, ColumnType.number, statisticalModel.getIndexColumn(), temporaryMemory.getThisTask().getEndIndex());
		}
		
		DataRange dataRange = dataSourceRepository.getDataRange(statisticalModel.getDbName(), sqlBuilder.toSql());
		if (dataRange != null) {
			temporaryMemory.addNextTask(dataRange, statisticalModel);
		}
		
		return temporaryMemory.hashThisTask() ? true : false;
	}
	
	@Override
	public void processed(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		StatisticalTask statisticalTask = temporaryMemory.getThisTask();
		Database database = dataSourceRepository.findDatabase(statisticalModel.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), statisticalModel.getFetchSql());
		sqlBuilder.addCondition(Operators.greater_equal, ColumnType.number, "id", statisticalTask.getStartIndex());
		sqlBuilder.addCondition(Operators.less_equal, ColumnType.number, "id", statisticalTask.getEndIndex());
		temporaryMemory.addMetaData(dataSourceRepository.querySourceData(statisticalModel.getDbName(), sqlBuilder.toSql()));
	}
	
	@Override
	public int getOrder() {
		return 1;
	}
}
