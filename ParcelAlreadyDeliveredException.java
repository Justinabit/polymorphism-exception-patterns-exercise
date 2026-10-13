// Thrown when an action is not allowed because the parcel is already delivered
public class ParcelAlreadyDeliveredException extends Exception {

    private static final long serialVersionUID = 1L;

    // Creates the exception with a message
    public ParcelAlreadyDeliveredException(String message) {
        super(message);
    }
}
