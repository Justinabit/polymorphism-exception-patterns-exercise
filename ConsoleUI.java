// Helper class for printing: headings, messages and number formats in one consistent style
public class ConsoleUI {

    private static final int LINE_WIDTH = 60;
    private static final String CURRENCY = "Php ";

    // Prints a section heading, e.g. "===== ADD PARCEL ====="
    public static void printSection(String title) {
        System.out.println();
        System.out.println("=".repeat(LINE_WIDTH));
        System.out.println("  " + title);
        System.out.println("=".repeat(LINE_WIDTH));
    }

    // Prints a smaller heading inside a section, e.g. "-- Parcel Details --"
    public static void printSubheading(String title) {
        System.out.println();
        System.out.println("  -- " + title + " " + "-".repeat(LINE_WIDTH - title.length() - 6));
    }

    // Prints a message for an action that worked
    public static void printSuccess(String message) {
        System.out.println("  [OK] " + message);
    }

    // Prints a message for invalid input or an action that failed
    public static void printError(String message) {
        System.out.println("  [!] " + message);
    }

    // Prints a message that is only for information
    public static void printInfo(String message) {
        System.out.println("  [i] " + message);
    }

    // Prints a question and keeps the cursor on the same line for the answer
    public static void prompt(String question) {
        System.out.print("  " + question + ": ");
    }

    // Prints a "label : value" line so that all the values line up
    public static void printField(String label, String value) {
        System.out.printf("  %-18s: %s%n", label, value);
    }

    // Formats a fee as "Php 1,234.50"
    public static String formatFee(double fee) {
        return CURRENCY + String.format("%,.2f", fee);
    }

    // Cuts a long text and ends it with "..." so it fits in a table column
    public static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
