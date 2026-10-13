import java.util.List;

// Interface listing the operations a parcel manager must provide
public interface ParcelOperations {

    // Adds a parcel unless its tracking number already exists
    void addParcel(Parcel parcel) throws DuplicateTrackingNumberException;

    // Finds a parcel by tracking number
    Parcel findParcel(String trackingNumber) throws ParcelNotFoundException;

    // Changes the delivery service of a pending parcel
    void changeService(String trackingNumber, DeliveryStrategy strategy)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException,
            SameDayNotAllowedException;

    // Marks a pending parcel as delivered
    void markDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException;

    // Returns all parcels
    List<Parcel> getAllParcels();
}
