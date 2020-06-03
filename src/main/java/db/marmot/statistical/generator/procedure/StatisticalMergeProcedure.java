package db.marmot.statistical.generator.procedure;

import db.marmot.converter.AggregatesConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.AggregateColumn;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.StatisticalDistinct;
import db.marmot.statistical.generator.storage.StatisticalStorage;

import java.util.Set;

/**
 * @author shaokang
 */
public class StatisticalMergeProcedure implements StatisticalProcedure {
	
	private StatisticalStorage statisticalStorage;
	private DataSourceRepository dataSourceRepository;
	
	public StatisticalMergeProcedure(StatisticalStorage statisticalStorage, DataSourceRepository dataSourceRepository) {
		this.statisticalStorage = statisticalStorage;
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	public boolean match() {
		return statisticalStorage.hashMemoryDistinct() || statisticalStorage.hashMemoryStatistics();
	}
	
	@Override
	public void processed() {
		statisticalStorage.getStatisticalModels().forEach(statisticalModel -> {
			if (statisticalStorage.hashMemoryDistinct()) {
				Set<String> rowKeys = statisticalStorage.getMemoryDistinct().keySet();
				for (String rowKey : rowKeys) {
					StatisticalDistinct memoryDistinct = statisticalStorage.getStatisticalDistinct(rowKey);
					StatisticalDistinct dbDistinct = dataSourceRepository.findStatisticalDistinct(memoryDistinct.getRowKey(), memoryDistinct.getDistinctColumn());
					if (dbDistinct != null) {
						memoryDistinct.getDistinctData().addAll(dbDistinct.getDistinctData());
					}
				}
			}
			if (statisticalStorage.hashMemoryStatistics()) {
				Set<String> rowKeys = statisticalStorage.getMemoryStatistics().keySet();
				for (String rowKey : rowKeys) {
					StatisticalData dbData = dataSourceRepository.findStatisticalData(statisticalModel.getModelName(), rowKey);
					if (dbData != null) {
						for (AggregateColumn column : statisticalModel.getAggregateColumns()) {
							AggregatesConverter aggregatesConverter = ConverterAdapter.getInstance().getAggregatesConverter(column.getAggregates());
							aggregatesConverter.calculate(statisticalStorage, rowKey, column.getColumnCode(), dbData);
						}
					}
				}
			}
		});
	}
	
	@Override
	public StatisticalStorage statisticalStorage() {
		return statisticalStorage;
	}
}
