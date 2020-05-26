package db.marmot.statistical.generator.procedure;

import db.marmot.converter.AggregatesConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.AggregateColumn;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.StatisticalDistinct;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.generator.memory.TemporaryMemory;

import java.util.Set;

/**
 * @author shaokang
 */
public class StatisticalDataMergeProcedure implements StatisticalProcedure {
	
	private DataSourceRepository dataSourceRepository;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();

	public StatisticalDataMergeProcedure(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	public boolean match(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		return true;
	}
	
	@Override
	public void processed(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		
		if (temporaryMemory.hashMemoryDistinct()) {
			Set<String> rowKeys = temporaryMemory.getMemoryDistinct().keySet();
			for (String rowKey : rowKeys) {
				StatisticalDistinct memoryDistinct = temporaryMemory.getStatisticalDistinct(rowKey);
				StatisticalDistinct dbDistinct = dataSourceRepository.findStatisticalDistinct(memoryDistinct.getRowKey(), memoryDistinct.getDistinctColumn());
				if (dbDistinct != null) {
					memoryDistinct.getDistinctData().addAll(dbDistinct.getDistinctData());
				}
			}
		}
		
		if (temporaryMemory.hashMemoryStatistics()) {
			Set<String> rowKeys = temporaryMemory.getMemoryStatistics().keySet();
			for (String rowKey : rowKeys) {
				StatisticalData dbData = dataSourceRepository.findStatisticalData(statisticalModel.getModelName(), rowKey);
				if (dbData != null) {
					for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
						AggregatesConverter aggregatesConverter = converterAdapter.getAggregatesConverter(column.getAggregates());
						aggregatesConverter.calculate(temporaryMemory, rowKey, column.getColumnCode(), dbData);
					}
				}
			}
		}
	}
	
	@Override
	public int getOrder() {
		return 3;
	}
}
