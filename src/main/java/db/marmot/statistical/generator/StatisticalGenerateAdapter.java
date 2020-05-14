package db.marmot.statistical.generator;

import java.util.Date;
import java.util.List;
import java.util.Map;

import db.marmot.repository.RepositoryAdapter;

/**
 * @author shaokang
 */
public interface StatisticalGenerateAdapter {
	
	void setMaxPoolSize(int maxPoolSize);

	void setRepositoryAdapter(RepositoryAdapter repositoryAdapter);
	
	/**
	 * 生成统计数据
	 */
	void generateStatisticalData();
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(String modelName, Map<String, Object> groupData);
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param timeValue 时间值
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(String modelName, Date timeValue, Map<String, Object> groupData);
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param offset 偏移量
	 * @param timeValue 时间值
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(String modelName, int offset, Date timeValue, Map<String, Object> groupData);
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param offset 偏移量
	 * @param timeGroupData 时间值
	 * @return
	 */
	List<Map<String, Object>> getAggregateData(String modelName, int offset, Map<Date, Map<String, Object>> timeGroupData);
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(String modelName, Date startTime, Date endTime, Map<String, Object> groupData);
	
	/**
	 * 获取聚合数据
	 * @param modelName 模型名称
	 * @param offset 偏移量
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(String modelName, int offset, Date startTime, Date endTime, Map<String, Object> groupData);
	
	/**
	 * 获取聚合数据
	 * @param modelNames 模型名称
	 * @param offset 偏移量
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param groupData 分组数据
	 * @return
	 */
	Map<String, Object> getAggregateData(List<String> modelNames, int offset, Date startTime, Date endTime, Map<String, Object> groupData);
	
}
