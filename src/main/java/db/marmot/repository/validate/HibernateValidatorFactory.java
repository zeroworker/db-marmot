package db.marmot.repository.validate;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * @author shaokang
 */
public class HibernateValidatorFactory {
	
	private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
	
	public static ValidatorFactory getInstance() {
		return validatorFactory;
	}
}
