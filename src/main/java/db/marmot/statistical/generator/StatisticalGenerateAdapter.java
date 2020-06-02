package db.marmot.statistical.generator;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public interface StatisticalGenerateAdapter {
	
	/**
	 * 生成统计数据 定时任务执行 任务时间间隔根据实际业务场景定义
	 */
	void generateStatisticalData();
	
	/**
	 * 回滚统计数据 存在元数据订正任务时 定时任务执行
	 */
	void rollbackStatisticalData();
	
	/**
	 * 订正统计数据 元数据订正完成,根据指定的任务ID执行数据订正处理
	 * @param taskId
	 */
	void reviseStatisticalData(long taskId);
	
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
