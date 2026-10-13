// Enum for the two parcel statuses, with a display name like "Pending"
public enum ParcelStatus {
    PENDING("Pending"),
    DELIVERED("Delivered");

    private final String displayName;

    // Creates a status with its display name
    ParcelStatus(String displayName) {
        this.displayName = displayName;
    }

    // Returns the display name
    public String getDisplayName() {
        return displayName;
    }

    // Prints the status using its display name
    @Override
    public String toString() {
        return displayName;
    }
}
