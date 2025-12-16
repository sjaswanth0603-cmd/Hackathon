

public class BedUnavailableException extends Exception {
    public BedUnavailableException(String message) {
        super(message);
    }

    public BedUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}