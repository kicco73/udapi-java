package cnr.ilc.rut;

/**
 * General Udapi exception.
 *
 * All Rut exceptions should extend this general exception.
 *
 * @author Enrico Carniani
 */
public class RutException extends RuntimeException {
    /**
     * Default constructor.
     */
    public RutException() {
        super();
    }

    public RutException(String message) {
        super(message);
    }

    public RutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RutException(Throwable cause) {
        super(cause);
    }

    protected RutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
