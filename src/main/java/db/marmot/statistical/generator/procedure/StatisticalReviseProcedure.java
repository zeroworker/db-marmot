package db.marmot.statistical.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.Operators;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.StatisticalReviseTask;
import db.marmot.statistical.generator.storage.StatisticalReviseStorage;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;

/**
 * @author shaokang
 */
public class StatisticalReviseProcedure extends StatisticalProcedureWrapper {
	
	private DataSourceRepository dataSourceRepository;
	
	public StatisticalReviseProcedure(StatisticalProcedure statisticalProcedure, DataSourceRepository dataSourceRepository) {
		super(statisticalProcedure);
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	public boolean match() {
		StatisticalReviseStorage statisticalStorage = statisticalStorage();
		return statisticalStorage.hasReviseTask();
	}
	
	@Override
	public void processed() {
		StatisticalReviseStorage statisticalStorage = statisticalStorage();
		DataVolume dataVolume = statisticalStorage.getDataVolume();
		DataColumn indexDataColumn = dataVolume.findIndexDataColumn();
		StatisticalReviseTask reviseTask = statisticalStorage.getReviseTask();
		Database database = dataSourceRepository.findDatabase(dataVolume.getDbName());
		
		SelectSqlBuilderConverter sqlBuilder = ConverterAdapter.getInstance().newInstanceSqlBuilder(database.getDbType(), dataVolume.getSqlScript());
		sqlBuilder.addCondition(Operators.less_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), reviseTask.getEndIndex());
		sqlBuilder.addCondition(Operators.greater_equal, indexDataColumn.getColumnType(), indexDataColumn.getColumnCode(), reviseTask.getStartIndex());
		statisticalStorage.addMetaData(dataSourceRepository.querySourceData(database.getName(), sqlBuilder.toSql()));
		super.processed();
	}
}
