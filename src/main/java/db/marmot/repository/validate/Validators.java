package db.marmot.repository.validate;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import org.apache.commons.lang3.Validate;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.util.Objects;
import java.util.Set;

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
	
	public static void validateSqlSelect(String sqlType, String sql) {
		notNull(sql, "sql 不能为空");
		notNull(sqlType, "sqlType 不能为空");
		SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(sql, sqlType);
		if (sqlStatementParser.getLexer().token() != Token.SELECT) {
			throw new ValidateException("sql 必须为select sql");
		}
	}
	
	public static void isTrue(boolean expression, ValidateCallBack callBack) {
		if (expression) {
			callBack.validate();
		}
	}
	
	public interface ValidateCallBack {
		void validate();
	}
}