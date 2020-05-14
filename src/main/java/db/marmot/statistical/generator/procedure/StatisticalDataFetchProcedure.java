package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.*;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalRepository;
import db.marmot.statistical.StatisticalTask;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.DataBaseRepository;
import db.marmot.volume.DataRange;
import db.marmot.volume.Database;

import lombok.extern.slf4j.Slf4j;

/**
 * @author shaokang
 */
@Slf4j
public class StatisticalDataFetchProcedure implements StatisticalProcedure {
	
	private DataBaseRepository dataBaseRepository;
	private StatisticalRepository statisticalRepository;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public StatisticalDataFetchProcedure(RepositoryAdapter repositoryAdapter) {
		this.dataBaseRepository = repositoryAdapter.getRepository(RepositoryType.database);
		this.statisticalRepository = repositoryAdapter.getRepository(RepositoryType.statistical);
	}
	
	@Override
	public boolean match(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		Database database = dataBaseRepository.findDatabase(statisticalModel.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), statisticalModel.getFetchSql());
		sqlBuilder.addSelectAggregateItem(Aggregates.min, "id", "minValue").addSelectAggregateItem(Aggregates.max, "id", "maxValue");
		
		temporaryMemory.addThisTask(statisticalRepository.findStatisticalTask(statisticalModel.getModelName()));
		if (temporaryMemory.hashThisTask()) {
			sqlBuilder.addCondition(Operators.greater_than, ColumnType.number, "id", temporaryMemory.getThisTask().getEndIndex());
		}
		
		DataRange dataRange = dataBaseRepository.getDataRange(statisticalModel.getDbName(),sqlBuilder.toSql());
		if (dataRange != null) {
			temporaryMemory.addNextTask(dataRange, statisticalModel);
		}
		
		return temporaryMemory.hashThisTask() ? true : false;
	}
	
	@Override
	public void processed(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		StatisticalTask statisticalTask = temporaryMemory.getThisTask();
		Database database = dataBaseRepository.findDatabase(statisticalModel.getDbName());
		SelectSqlBuilderConverter sqlBuilder = converterAdapter.newInstanceSqlBuilder(database.getDbType(), statisticalModel.getFetchSql());
		sqlBuilder.addCondition(Operators.greater_equal, ColumnType.number, "id", statisticalTask.getStartIndex());
		sqlBuilder.addCondition(Operators.less_equal, ColumnType.number, "id", statisticalTask.getEndIndex());
		temporaryMemory.addMetaData(dataBaseRepository.queryData(statisticalModel.getDbName(), sqlBuilder.toSql()));
	}
	
	@Override
	public int getOrder() {
		return 1;
	}
}
