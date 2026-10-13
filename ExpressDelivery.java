// Express delivery: the fee is the base fee x 1.5
public class ExpressDelivery implements DeliveryStrategy {

    // Fee is the base fee multiplied by 1.5
    @Override
    public double calculateFee(Parcel parcel) {
        return parcel.getBaseFee() * 1.5;
    }

    // Express delivery is always allowed
    @Override
    public boolean isAllowed(Parcel parcel) {
        return true;
    }

    // Returns the service name
    @Override
    public String getName() {
        return "Express";
    }
}
