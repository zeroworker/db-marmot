package db.marmot.statistical.generator.procedure;

import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.storage.StatisticalStorage;

/**
 * @author shaokang
 */
public abstract class StatisticalProcedureWrapper implements StatisticalProcedure {
	
	private StatisticalProcedure statisticalProcedure;
	
	public StatisticalProcedureWrapper(StatisticalProcedure statisticalProcedure) {
		Validators.notNull(statisticalProcedure, "statisticalProcedure不能为空");
		this.statisticalProcedure = statisticalProcedure;
	}
	
	@Override
	public void processed() {
		if (statisticalProcedure.match()) {
			statisticalProcedure.processed();
		}
	}
	
	@Override
	public <T extends StatisticalStorage> T statisticalStorage() {
		return statisticalProcedure.statisticalStorage();
	}
}
