package kiruto.rpcserver.exception;

public class SkException extends RuntimeException {

    public SkException(String message) {
        super(message);
    }

    public SkException(String message, Throwable cause) {
        super(message, cause);
    }
}
