package db.marmot.statistical.generator.procedure;

import java.util.Set;

import db.marmot.converter.AggregatesConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.statistical.*;
import db.marmot.statistical.generator.memory.TemporaryMemory;

/**
 * @author shaokang
 */
public class StatisticalDataMergeProcedure implements StatisticalProcedure {
	
	private StatisticalRepository statisticalRepository;
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	public StatisticalDataMergeProcedure(StatisticalRepository statisticalRepository) {
		this.statisticalRepository = statisticalRepository;
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
				StatisticalDistinct dbDistinct = statisticalRepository.findStatisticalDistinct(memoryDistinct.getRowKey(), memoryDistinct.getDistinctColumn());
				if (dbDistinct != null) {
					memoryDistinct.getDistinctData().addAll(dbDistinct.getDistinctData());
				}
			}
		}
		
		if (temporaryMemory.hashMemoryStatistics()) {
			Set<String> rowKeys = temporaryMemory.getMemoryStatistics().keySet();
			for (String rowKey : rowKeys) {
				StatisticalData dbData = statisticalRepository.findStatisticalData(statisticalModel.getModelName(), rowKey);
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
