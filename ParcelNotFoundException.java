// Thrown when no parcel matches the tracking number
public class ParcelNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    // Creates the exception with a message
    public ParcelNotFoundException(String message) {
        super(message);
    }
}
