// Thrown when a tracking number already exists
public class DuplicateTrackingNumberException extends Exception {

    private static final long serialVersionUID = 1L;

    // Creates the exception with a message
    public DuplicateTrackingNumberException(String message) {
        super(message);
    }
}
