

public class InvalidPatientStateException extends RuntimeException {
    public InvalidPatientStateException(String message) {
        super(message);
    }

    public InvalidPatientStateException(String message, Throwable cause) {
        super(message, cause);
    }
}