package db.marmot.statistical.generator.procedure;

import db.marmot.statistical.generator.storage.StatisticalStorage;

/**
 * @author shaokang
 */
public interface StatisticalProcedure {
	
	boolean match();
	
	void processed();
	
	<T extends StatisticalStorage> T statisticalStorage();
}
