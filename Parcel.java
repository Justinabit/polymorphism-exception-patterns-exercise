// Abstract base class for all parcels; subclasses define the weight and fee rules
public abstract class Parcel {

    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private DeliveryStrategy strategy;
    private ParcelStatus status;

    // Creates a parcel that always starts as Pending
    public Parcel(String trackingNumber, String receiverName, double weight,
                  DeliveryStrategy strategy) {
        this.trackingNumber = trackingNumber;
        this.receiverName = receiverName;
        this.weight = weight;
        this.strategy = strategy;
        this.status = ParcelStatus.PENDING;
    }

    // Returns the weight used for pricing
    public abstract double getChargeableWeight();

    // Returns the base fee before the delivery service is applied
    public abstract double getBaseFee();

    // Returns the parcel type name
    public abstract String getType();

    // Returns the maximum allowed weight for this parcel type
    public abstract double getMaxWeight();

    // Returns extra information that depends on the parcel type (pages, size, note)
    public abstract String getDetails();

    // Returns the chargeable weight as text (whole number by default)
    public String getChargeableWeightText() {
        return String.format("%.0f", getChargeableWeight());
    }

    // Returns the delivery fee using the current delivery strategy
    public double getDeliveryFee() {
        return strategy.calculateFee(this);
    }

    // Replaces the delivery strategy only if the new service is allowed for this parcel
    public void setStrategy(DeliveryStrategy newStrategy) throws SameDayNotAllowedException {
        if (!newStrategy.isAllowed(this)) {
            throw new SameDayNotAllowedException();
        }
        this.strategy = newStrategy;
    }

    // Marks the parcel as Delivered
    public void markDelivered() {
        this.status = ParcelStatus.DELIVERED;
    }

    // Checks if the parcel is already delivered
    public boolean isDelivered() {
        return status == ParcelStatus.DELIVERED;
    }

    // Returns the tracking number
    public String getTrackingNumber() {
        return trackingNumber;
    }

    // Returns the receiver name
    public String getReceiverName() {
        return receiverName;
    }

    // Returns the actual weight in kg
    public double getWeight() {
        return weight;
    }

    // Returns the current delivery strategy
    public DeliveryStrategy getStrategy() {
        return strategy;
    }

    // Returns the current status
    public ParcelStatus getStatus() {
        return status;
    }
}
