// Fragile parcel: a Box with a handling note, a lower max weight and a handling fee
public class Fragile extends Box {

    public static final double MAX_WEIGHT = 30.0;
    public static final double HANDLING_FEE = 100.0;
    public static final int MAX_NOTE_LENGTH = 60;

    private final String handlingNote;

    // Creates a fragile parcel
    public Fragile(String trackingNumber, String receiverName, double weight,
                   DeliveryStrategy strategy, int length, int width, int height,
                   String handlingNote) {
        super(trackingNumber, receiverName, weight, strategy, length, width, height);
        this.handlingNote = handlingNote;
    }

    // Base fee is the Box base fee plus the Php 100 handling fee
    @Override
    public double getBaseFee() {
        return super.getBaseFee() + HANDLING_FEE;
    }

    // A fragile parcel's details are the Box details plus the handling note
    @Override
    public String getDetails() {
        return super.getDetails() + ", note: " + handlingNote;
    }

    // Returns the type name
    @Override
    public String getType() {
        return "Fragile";
    }

    // Returns the maximum weight of a fragile parcel
    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT;
    }

    // Returns the handling note
    public String getHandlingNote() {
        return handlingNote;
    }
}
