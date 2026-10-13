// Strategy interface: each delivery service calculates its own fee
public interface DeliveryStrategy {

    // Calculates the delivery fee for a parcel
    double calculateFee(Parcel parcel);

    // Checks if this service can be used for a parcel
    boolean isAllowed(Parcel parcel);

    // Returns the service name
    String getName();
}
