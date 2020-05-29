package db.marmot.statistical.generator.memory;

import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.StatisticalDistinct;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalTask;
import db.marmot.volume.DataRange;
import db.marmot.volume.DataVolume;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public interface TemporaryMemory {

	void addThisTask(StatisticalTask statisticalTask);
	
	void addNextTask(DataRange dataRange, DataVolume dataVolume, StatisticalModel statisticalModel);
	
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
