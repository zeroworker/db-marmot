package db.marmot.graphic.generator;

/**
 * @author shaokang
 */
public class GraphicGeneratorException extends RuntimeException {
    public GraphicGeneratorException() {
    }

    public GraphicGeneratorException(String message) {
        super(message);
    }

    public GraphicGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphicGeneratorException(Throwable cause) {
        super(cause);
    }

    public GraphicGeneratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
