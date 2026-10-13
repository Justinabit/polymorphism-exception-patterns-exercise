// Same Day delivery: the fee is (base fee x 2) + Php 100, only for parcels up to 10 kg
public class SameDayDelivery implements DeliveryStrategy {

    private static final double MAX_CHARGEABLE_WEIGHT = 10.0;

    // Fee is double the base fee plus Php 100
    @Override
    public double calculateFee(Parcel parcel) {
        return (parcel.getBaseFee() * 2) + 100.0;
    }

    // Allowed only when the chargeable weight is 10 kg or less
    @Override
    public boolean isAllowed(Parcel parcel) {
        return parcel.getChargeableWeight() <= MAX_CHARGEABLE_WEIGHT;
    }

    // Returns the service name
    @Override
    public String getName() {
        return "Same Day";
    }
}
