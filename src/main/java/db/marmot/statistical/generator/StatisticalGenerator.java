package db.marmot.statistical.generator;

import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalReviseTask;

import java.util.List;

/**
 * @author shaokang
 */
public interface StatisticalGenerator {
	
	/**
	 * 执行统计模型-统计数据
	 * @param statisticalModels
	 */
	void execute(List<StatisticalModel> statisticalModels);
	
	/**
	 * 回滚统计模型-统计数据
	 * @param statisticalModels
	 * @param reviseTask
	 */
	void rollBack(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask);
	
	/**
	 * 订正统计模型-统计数据
	 * @param statisticalModels
	 * @param reviseTask
	 */
	void revise(List<StatisticalModel> statisticalModels, StatisticalReviseTask reviseTask);
}
