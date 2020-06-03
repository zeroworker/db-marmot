package db.marmot.statistical.generator.storage;

import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalData;
import db.marmot.statistical.StatisticalDistinct;
import db.marmot.statistical.StatisticalException;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataVolume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public abstract class StatisticalStorage {
	
	private DataVolume dataVolume;
	private List<Map<String, Object>> metaData;
	private Map<String, StatisticalData> memoryStatistics;
	private Map<String, StatisticalDistinct> memoryDistinct;

	public StatisticalStorage(DataVolume dataVolume) {
		Validators.notNull(dataVolume,"数据集不能为空");
		this.dataVolume = dataVolume;
	}

	public void addMetaData(List<Map<String, Object>> metaData) {
		if (this.metaData != null) {
			throw new StatisticalException("源数据已经存在");
		}
		this.metaData = metaData;
	}
	
	public void addStatisticalData(StatisticalData statisticalData) {
		if (memoryStatistics == null) {
			memoryStatistics = new HashMap<>();
		}
		memoryStatistics.put(statisticalData.getRowKey(), statisticalData);
	}
	
	public void addStatisticalDistinct(StatisticalDistinct statisticalDistinct) {
		if (memoryDistinct == null) {
			memoryDistinct = new HashMap<>();
		}
		memoryDistinct.put(statisticalDistinct.uniqueKey(), statisticalDistinct);
	}
	
	public boolean hashMetaData() {
		return this.metaData != null && !this.metaData.isEmpty();
	}
	
	public List<Map<String, Object>> getMetaData() {
		return this.metaData;
	}
	
	public boolean hashStatisticalData(String rowKey) {
		return hashMemoryStatistics() && memoryStatistics.get(rowKey) != null;
	}
	
	public boolean hashMemoryStatistics() {
		return this.memoryStatistics != null && !this.memoryStatistics.isEmpty();
	}
	
	public Map<String, StatisticalData> getMemoryStatistics() {
		return this.memoryStatistics;
	}
	
	public StatisticalData getStatisticalData(String rowKey) {
		return memoryStatistics.get(rowKey);
	}
	
	public boolean hashStatisticalDistinct(String rowKey) {
		return hashMemoryDistinct() && memoryDistinct.get(rowKey) != null;
	}
	
	public boolean hashMemoryDistinct() {
		return this.memoryDistinct != null && !this.memoryDistinct.isEmpty();
	}
	
	public Map<String, StatisticalDistinct> getMemoryDistinct() {
		return this.memoryDistinct;
	}
	
	public StatisticalDistinct getStatisticalDistinct(String rowKey) {
		return memoryDistinct.get(rowKey);
	}
	
	public DataVolume getDataVolume() {
		return dataVolume;
	}

	public abstract List<StatisticalModel> getStatisticalModels();
}
