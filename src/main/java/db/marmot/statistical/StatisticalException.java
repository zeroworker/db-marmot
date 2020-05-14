package db.marmot.statistical;

/**
 * @author shaokang
 */
public class StatisticalException extends RuntimeException {
	public StatisticalException() {
	}
	
	public StatisticalException(String message) {
		super(message);
	}
	
	public StatisticalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public StatisticalException(Throwable cause) {
		super(cause);
	}
	
	public StatisticalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
