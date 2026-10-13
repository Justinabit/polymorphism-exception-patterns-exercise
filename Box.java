// Box parcel: priced by chargeable weight (the bigger of actual and volumetric weight)
public class Box extends Parcel {

    public static final double MAX_WEIGHT = 50.0;

    private final int length;
    private final int width;
    private final int height;

    // Creates a box parcel
    public Box(String trackingNumber, String receiverName, double weight,
               DeliveryStrategy strategy, int length, int width, int height) {
        super(trackingNumber, receiverName, weight, strategy);
        this.length = length;
        this.width = width;
        this.height = height;
    }

    // Calculates the volumetric weight using doubles so large sizes never overflow
    public double getVolumetricWeight() {
        return (double) length * width * height / 3500.0;
    }

    // Chargeable weight is the bigger weight rounded up to the next whole kg
    @Override
    public double getChargeableWeight() {
        return Math.ceil(Math.max(getWeight(), getVolumetricWeight()));
    }

    // Base fee is Php 40 per chargeable kg
    @Override
    public double getBaseFee() {
        return 40.0 * getChargeableWeight();
    }

    // Returns the type name
    @Override
    public String getType() {
        return "Box";
    }

    // Returns the maximum weight of a box
    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT;
    }

    // Returns the length in cm
    public int getLength() {
        return length;
    }

    // Returns the width in cm
    public int getWidth() {
        return width;
    }

    // Returns the height in cm
    public int getHeight() {
        return height;
    }
}
