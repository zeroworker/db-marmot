package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Aggregates;
import db.marmot.enums.Operators;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTask;
import db.marmot.statistical.generator.storage.StatisticalDefaultStorage;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataRange;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;

/**
 * @author shaokang
 */
public class StatisticalDefaultProcedure extends StatisticalProcedureWrapper {
	
	private DataSourceRepository dataSourceRepository;
	
	public StatisticalDefaultProcedure(StatisticalProcedure statisticalProcedure, DataSourceRepository dataSourceRepository) {
		super(statisticalProcedure);
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	public boolean match() {
		StatisticalDefaultStorage statisticalStorage = statisticalStorage();
		DataVolume dataVolume = statisticalStorage.getDataVolume();
		DataColumn indexDataColumn = dataVolume.findIndexDataColumn();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		
		SelectSqlBuilderConverter sqlBuilder = ConverterAdapter.getInstance().newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		sqlBuilder.addSelectAggregateItem(Aggregates.min, indexDataColumn.getColumnCode(), "minValue").addSelectAggregateItem(Aggregates.max, indexDataColumn.getColumnCode(), "maxValue");
		StatisticalModel statisticalModel = statisticalStorage.getStatisticalModel();
		
		statisticalStorage.addThisTask(dataSourceRepository.findStatisticalTask(statisticalModel.getModelName()));
		if (statisticalStorage.hashThisTask()) {
			sqlBuilder.addCondition(Operators.greater_than, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), statisticalStorage.getThisTask().getEndIndex());
		}
		
		DataRange dataRange = dataSourceRepository.getDataRange(database.getName(), sqlBuilder.toSql());
		if (dataRange != null) {
			statisticalStorage.addNextTask(dataRange, dataVolume, statisticalModel);
		}
		
		return statisticalStorage.hashThisTask();
	}
	
	@Override
	public void processed() {
		StatisticalDefaultStorage statisticalStorage = statisticalStorage();
		
		DataVolume dataVolume = statisticalStorage.getDataVolume();
		DataColumn indexDataColumn = dataVolume.findIndexDataColumn();
		StatisticalTask statisticalTask = statisticalStorage.getThisTask();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		
		SelectSqlBuilderConverter sqlBuilder = ConverterAdapter.getInstance().newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		sqlBuilder.addCondition(Operators.greater_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), statisticalTask.getStartIndex());
		sqlBuilder.addCondition(Operators.less_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), statisticalTask.getEndIndex());
		statisticalStorage.addMetaData(dataSourceRepository.querySourceData(database.getName(), sqlBuilder.toSql()));
		super.processed();
	}
}
