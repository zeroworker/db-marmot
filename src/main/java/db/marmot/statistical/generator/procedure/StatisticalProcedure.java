package db.marmot.statistical.generator.procedure;

import org.springframework.core.Ordered;

import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.generator.memory.TemporaryMemory;

/**
 * @author shaokang
 */
public interface StatisticalProcedure extends Ordered {
	
	/**
	 * 匹配
	 * @param statisticalModel
	 * @param temporaryMemory
	 * @return
	 */
	boolean match(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory);
	
	/**
	 * 处理
	 * @param statisticalModel
	 * @param temporaryMemory
	 */
	void processed(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory);
}
