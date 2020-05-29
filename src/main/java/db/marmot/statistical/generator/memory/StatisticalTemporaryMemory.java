package db.marmot.statistical.generator.memory;

import db.marmot.statistical.*;
import db.marmot.volume.DataRange;
import db.marmot.volume.DataVolume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class StatisticalTemporaryMemory implements TemporaryMemory {
	
	private StatisticalTask thisTask;
	private StatisticalTask nextTask;
	private List<Map<String, Object>> metaData;
	private Map<String, StatisticalData> memoryStatistics;
	private Map<String, StatisticalDistinct> memoryDistinct;
	
	@Override
	public void addThisTask(StatisticalTask statisticalTask) {
		if (this.thisTask != null) {
			throw new StatisticalException("本次统计任务已经存在");
		}
		this.thisTask = statisticalTask;
	}
	
	@Override
	public void addNextTask(DataRange dataRange, DataVolume dataVolume, StatisticalModel statisticalModel) {
		if (this.nextTask != null) {
			throw new StatisticalException("下次统计任务已经存在");
		}
		
		StatisticalTask statisticalTask = new StatisticalTask();
		statisticalTask.setScanned(false);
		statisticalTask.setStartIndex(dataRange.getMinValue());
		statisticalTask.setModelName(statisticalModel.getModelName());
		statisticalTask.setEndIndex(dataRange.calculateEndIndex(dataVolume.getVolumeLimit()));
		this.nextTask = statisticalTask;
	}
	
	@Override
	public void addMetaData(List<Map<String, Object>> metaData) {
		if (this.metaData != null) {
			throw new StatisticalException("源数据已经存在");
		}
		this.metaData = metaData;
	}
	
	@Override
	public void addStatisticalData(StatisticalData statisticalData) {
		if (memoryStatistics == null) {
			memoryStatistics = new HashMap<>();
		}
		memoryStatistics.put(statisticalData.getRowKey(), statisticalData);
	}
	
	@Override
	public void addStatisticalDistinct(StatisticalDistinct statisticalDistinct) {
		if (memoryDistinct == null) {
			memoryDistinct = new HashMap<>();
		}
		memoryDistinct.put(statisticalDistinct.uniqueKey(), statisticalDistinct);
	}
	
	@Override
	public boolean hashThisTask() {
		return this.thisTask != null && !this.thisTask.isScanned();
	}
	
	@Override
	public StatisticalTask getThisTask() {
		return this.thisTask;
	}
	
	@Override
	public boolean hashNextTask() {
		return this.nextTask != null;
	}
	
	@Override
	public StatisticalTask getNextTask() {
		return this.nextTask;
	}
	
	@Override
	public boolean hashMetaData() {
		return this.metaData != null && !this.metaData.isEmpty();
	}
	
	@Override
	public List<Map<String, Object>> getMetaData() {
		return this.metaData;
	}
	
	@Override
	public boolean hashStatisticalData(String rowKey) {
		return hashMemoryStatistics() && memoryStatistics.get(rowKey) != null;
	}
	
	@Override
	public boolean hashMemoryStatistics() {
		return this.memoryStatistics != null && !this.memoryStatistics.isEmpty();
	}
	
	@Override
	public Map<String, StatisticalData> getMemoryStatistics() {
		return this.memoryStatistics;
	}
	
	@Override
	public StatisticalData getStatisticalData(String rowKey) {
		return memoryStatistics.get(rowKey);
	}
	
	@Override
	public boolean hashStatisticalDistinct(String rowKey) {
		return hashMemoryDistinct() && memoryDistinct.get(rowKey) != null;
	}
	
	@Override
	public boolean hashMemoryDistinct() {
		return this.memoryDistinct != null && !this.memoryDistinct.isEmpty();
	}
	
	@Override
	public Map<String, StatisticalDistinct> getMemoryDistinct() {
		return this.memoryDistinct;
	}
	
	@Override
	public StatisticalDistinct getStatisticalDistinct(String rowKey) {
		return memoryDistinct.get(rowKey);
	}
}
