import java.util.ArrayList;
import java.util.List;

// Singleton class that owns and manages the list of all parcels
public class ParcelManager implements ParcelOperations {

    private static ParcelManager instance;

    private final ArrayList<Parcel> parcels;

    // Private constructor so no other class can create a ParcelManager
    private ParcelManager() {
        this.parcels = new ArrayList<>();
    }

    // Returns the only ParcelManager object, creating it the first time
    public static ParcelManager getInstance() {
        if (instance == null) {
            instance = new ParcelManager();
        }
        return instance;
    }

    // Checks if a tracking number is already used
    public boolean trackingExists(String trackingNumber) {
        for (Parcel parcel : parcels) {
            if (parcel.getTrackingNumber().equals(trackingNumber)) {
                return true;
            }
        }
        return false;
    }

    // Adds a parcel unless its tracking number already exists
    @Override
    public void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        if (trackingExists(parcel.getTrackingNumber())) {
            throw new DuplicateTrackingNumberException(
                    "Tracking number already exists! Please use a different one.");
        }
        parcels.add(parcel);
    }

    // Finds a parcel by tracking number
    @Override
    public Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        for (Parcel parcel : parcels) {
            if (parcel.getTrackingNumber().equals(trackingNumber)) {
                return parcel;
            }
        }
        throw new ParcelNotFoundException("Parcel not found!");
    }

    // Changes the delivery service of a pending parcel
    @Override
    public void changeService(String trackingNumber, DeliveryStrategy strategy)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException,
            SameDayNotAllowedException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel already delivered!");
        }
        parcel.setStrategy(strategy);
    }

    // Marks a pending parcel as delivered
    @Override
    public void markDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel was already delivered.");
        }
        parcel.markDelivered();
    }

    // Returns a copy of the list of all parcels
    @Override
    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels);
    }

    // Checks if there are no parcels
    public boolean isEmpty() {
        return parcels.isEmpty();
    }
}
