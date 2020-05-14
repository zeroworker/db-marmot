package db.marmot.graphic.download;


/**
 * @author shaokang
 */
public class GraphicDownloadException extends RuntimeException {

    public GraphicDownloadException() {
    }

    public GraphicDownloadException(String message) {
        super(message);
    }

    public GraphicDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphicDownloadException(Throwable cause) {
        super(cause);
    }

    public GraphicDownloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
