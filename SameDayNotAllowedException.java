// Thrown when Same Day delivery is chosen for a parcel that is too heavy
public class SameDayNotAllowedException extends Exception {

    private static final long serialVersionUID = 1L;

    // Creates the exception with the fixed Same Day message
    public SameDayNotAllowedException() {
        super("Same Day delivery is only for parcels up to 10 kg!");
    }
}
