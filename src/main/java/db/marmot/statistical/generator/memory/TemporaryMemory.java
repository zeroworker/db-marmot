package db.marmot.statistical.generator.memory;

import java.util.List;
import java.util.Map;

import db.marmot.statistical.*;
import db.marmot.volume.DataRange;

/**
 * @author shaokang
 */
public interface TemporaryMemory {

	void addThisTask(StatisticalTask statisticalTask);
	
	void addNextTask(DataRange dataRange, StatisticalModel statisticalModel);
	
	void addMetaData(List<Map<String, Object>> metaData);
	
	void addStatisticalData(StatisticalData statisticalData);
	
	void addStatisticalDistinct(StatisticalDistinct statisticalDistinct);
	
	boolean hashThisTask();
	
	StatisticalTask getThisTask();
	
	boolean hashNextTask();
	
	StatisticalTask getNextTask();
	
	boolean hashMetaData();
	
	List<Map<String, Object>> getMetaData();
	
	boolean hashStatisticalData(String rowKey);
	
	boolean hashMemoryStatistics();
	
	Map<String, StatisticalData> getMemoryStatistics();
	
	StatisticalData getStatisticalData(String rowKey);
	
	boolean hashStatisticalDistinct(String rowKey);
	
	boolean hashMemoryDistinct();
	
	Map<String, StatisticalDistinct> getMemoryDistinct();

	StatisticalDistinct getStatisticalDistinct(String rowKey);
}
