package db.marmot.repository.validate;

import java.util.Objects;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.apache.commons.lang3.Validate;

/**
 * @author shaokang
 */
public class Validators extends Validate {
	
	public static Object assertJSR303(Object object) {
		return assertJSR303(object, Default.class);
	}
	
	public static Object assertJSR303(Object object, Class<?>... groups) {
		return assertJSR303(object, null, groups);
	}
	
	public static Object assertJSR303(Object object, ValidatorFactory validatorFactory, Class<?>... groups) {
		if (validatorFactory == null) {
			validatorFactory = HibernateValidatorFactory.getInstance();
		}
		Objects.requireNonNull(object);
		Set<ConstraintViolation<Object>> constraintViolations = validatorFactory.getValidator().validate(object, groups);
		validateJsr303(constraintViolations);
		return object;
	}
	
	private static <T> void validateJsr303(Set<ConstraintViolation<T>> constraintViolations) {
		ValidateException exception = null;
		if (constraintViolations != null && !constraintViolations.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			for (ConstraintViolation<T> constraintViolation : constraintViolations) {
				errorMessage.append(constraintViolation.getPropertyPath().toString()).append(constraintViolation.getMessage()).append(";");
			}
			exception = new ValidateException(errorMessage.toString());
		}
		if (exception != null) {
			throw exception;
		}
	}
}