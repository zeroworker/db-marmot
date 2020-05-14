package db.marmot.volume.parser;

/**
 * @author shaokang
 */
public class SqlParserException extends RuntimeException{

    public SqlParserException() {
    }

    public SqlParserException(String message) {
        super(message);
    }

    public SqlParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlParserException(Throwable cause) {
        super(cause);
    }

    public SqlParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
