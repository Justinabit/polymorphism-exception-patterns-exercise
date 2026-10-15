// Document parcel: light weight with a flat Php 50 base fee
public class Document extends Parcel {

    public static final double MAX_WEIGHT = 0.5;
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;
    public static final double BASE_FEE = 50.0;

    private final int pages;

    // Creates a document parcel
    public Document(String trackingNumber, String receiverName, double weight,
                    DeliveryStrategy strategy, int pages) {
        super(trackingNumber, receiverName, weight, strategy);
        this.pages = pages;
    }

    // Chargeable weight of a document is its actual weight
    @Override
    public double getChargeableWeight() {
        return getWeight();
    }

    // Base fee of a document is a flat Php 50
    @Override
    public double getBaseFee() {
        return BASE_FEE;
    }

    // Returns the type name
    @Override
    public String getType() {
        return "Document";
    }

    // Returns the maximum weight of a document
    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT;
    }

    // A document's details are its number of pages
    @Override
    public String getDetails() {
        return pages + " page(s)";
    }

    // Shows a document's chargeable weight with 2 decimals
    @Override
    public String getChargeableWeightText() {
        return String.format("%.2f", getChargeableWeight());
    }

    // Returns the number of pages
    public int getPages() {
        return pages;
    }
}
