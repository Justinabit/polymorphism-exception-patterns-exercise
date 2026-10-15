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

    // Throws an exception if the tracking number is already used by another parcel
    public void checkNewTrackingNumber(String trackingNumber)
            throws DuplicateTrackingNumberException {
        if (trackingExists(trackingNumber)) {
            throw new DuplicateTrackingNumberException("Tracking number " + trackingNumber
                    + " already exists! Please use a different one.");
        }
    }

    // Adds a parcel unless its tracking number already exists
    @Override
    public void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        checkNewTrackingNumber(parcel.getTrackingNumber());
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
        throw new ParcelNotFoundException("Parcel " + trackingNumber + " was not found!");
    }

    // Finds a parcel that is not yet delivered (only these can still be changed)
    public Parcel findPendingParcel(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel " + trackingNumber
                    + " has already been delivered!");
        }
        return parcel;
    }

    // Changes the delivery service of a pending parcel
    @Override
    public void changeService(String trackingNumber, DeliveryStrategy strategy)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException,
            SameDayNotAllowedException {
        Parcel parcel = findPendingParcel(trackingNumber);
        parcel.setStrategy(strategy);
    }

    // Marks a pending parcel as delivered
    @Override
    public void markDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findPendingParcel(trackingNumber);
        parcel.markDelivered();
    }

    // Returns a copy of the list of all parcels
    @Override
    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels);
    }

    // Returns only the parcels that are not yet delivered
    public List<Parcel> getPendingParcels() {
        List<Parcel> pending = new ArrayList<>();
        for (Parcel parcel : parcels) {
            if (!parcel.isDelivered()) {
                pending.add(parcel);
            }
        }
        return pending;
    }

    // Checks if there are no parcels
    public boolean isEmpty() {
        return parcels.isEmpty();
    }
}
