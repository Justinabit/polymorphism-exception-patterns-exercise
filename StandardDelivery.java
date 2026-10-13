// Standard delivery: the fee is the base fee
public class StandardDelivery implements DeliveryStrategy {

    // Fee is the base fee
    @Override
    public double calculateFee(Parcel parcel) {
        return parcel.getBaseFee();
    }

    // Standard delivery is always allowed
    @Override
    public boolean isAllowed(Parcel parcel) {
        return true;
    }

    // Returns the service name
    @Override
    public String getName() {
        return "Standard";
    }
}
