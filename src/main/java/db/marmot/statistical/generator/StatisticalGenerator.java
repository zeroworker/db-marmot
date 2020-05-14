package db.marmot.statistical.generator;

import java.util.List;

import db.marmot.statistical.StatisticalModel;

/**
 * @author shaokang
 */
public interface StatisticalGenerator {
	
	/**
	 * 执行统计模型
	 * @param statisticalModels
	 */
	void execute(List<StatisticalModel> statisticalModels);
}
