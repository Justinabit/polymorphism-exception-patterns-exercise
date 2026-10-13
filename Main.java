import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Main class: shows the menu and handles all user input
public class Main {

    private static final String CURRENCY = "Php ";
    private static final int TABLE_WIDTH = 102;
    private static final int RECEIVER_DISPLAY_LENGTH = 15;

    // Starts the program and keeps showing the menu until the user exits
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ParcelManager manager = ParcelManager.getInstance();

        boolean running = true;
        try {
            while (running) {
                printMainMenu();
                int choice = readMenuChoice(scanner, 5);

                switch (choice) {
                    case 1:
                        handleAddParcel(scanner, manager);
                        break;
                    case 2:
                        handleViewAll(manager);
                        break;
                    case 3:
                        handleChangeService(scanner, manager);
                        break;
                    case 4:
                        handleMarkDelivered(scanner, manager);
                        break;
                    case 5:
                        running = false;
                        System.out.println();
                        System.out.println("Thank you for using the Parcel Delivery System!");
                        System.out.println("Goodbye!");
                        break;
                    default:
                        // readMenuChoice guarantees this is unreachable.
                        break;
                }
            }
        } catch (NoSuchElementException e) {
            // The input stream ended (Ctrl+D), so exit gracefully.
            System.out.println();
            System.out.println("Thank you for using the Parcel Delivery System!");
            System.out.println("Goodbye!");
        }

        scanner.close();
    }

    // ------------------------------------------------------------------
    // Menu display
    // ------------------------------------------------------------------

    // Prints the main menu
    private static void printMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("        PARCEL DELIVERY SYSTEM");
        System.out.println("========================================");
        System.out.println();
        System.out.println("1. Add Parcel");
        System.out.println("2. View All Parcels");
        System.out.println("3. Change Delivery Service");
        System.out.println("4. Mark as Delivered");
        System.out.println("5. Exit");
        System.out.println();
    }

    // ------------------------------------------------------------------
    // 1 - Add Parcel
    // ------------------------------------------------------------------

    // Handles the Add Parcel option
    private static void handleAddParcel(Scanner scanner, ParcelManager manager) {
        System.out.println();
        System.out.println("---------------- ADD PARCEL ----------------");

        System.out.println("Select parcel type:");
        System.out.println("1. Document");
        System.out.println("2. Box");
        System.out.println("3. Fragile");
        int type = readMenuChoice(scanner, 3);

        String trackingNumber = readNewTrackingNumber(scanner, manager);
        String receiver = readNonEmptyString(scanner, "Enter receiver name: ",
                "Receiver name cannot be empty!");

        Parcel parcel;
        if (type == 1) {
            double weight = readWeight(scanner, Document.MAX_WEIGHT,
                    "Document weight must not exceed 0.5 kg!");
            int pages = readWholeNumber(scanner, "Enter number of pages: ",
                    Document.MIN_PAGES, Document.MAX_PAGES,
                    "Pages must be a whole number from 1 to 100!");
            parcel = new Document(trackingNumber, receiver, weight, new StandardDelivery(), pages);
        } else if (type == 2) {
            double weight = readWeight(scanner, Box.MAX_WEIGHT,
                    "Box weight must not exceed 50 kg!");
            int length = readDimension(scanner, "Length");
            int width = readDimension(scanner, "Width");
            int height = readDimension(scanner, "Height");
            parcel = new Box(trackingNumber, receiver, weight, new StandardDelivery(),
                    length, width, height);
        } else {
            double weight = readWeight(scanner, Fragile.MAX_WEIGHT,
                    "Fragile weight must not exceed 30 kg!");
            int length = readDimension(scanner, "Length");
            int width = readDimension(scanner, "Width");
            int height = readDimension(scanner, "Height");
            String note = readNonEmptyString(scanner, "Enter handling note: ",
                    "Handling note cannot be empty!");
            parcel = new Fragile(trackingNumber, receiver, weight, new StandardDelivery(),
                    length, width, height, note);
        }

        // Keep asking for the service until a valid one is chosen
        while (true) {
            DeliveryStrategy strategy = readService(scanner);
            try {
                parcel.setStrategy(strategy);
                manager.addParcel(parcel);
                System.out.println("Parcel added successfully! Delivery fee: "
                        + formatFee(parcel.getDeliveryFee()));
                return;
            } catch (SameDayNotAllowedException e) {
                System.out.println(e.getMessage());
            } catch (DuplicateTrackingNumberException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // 2 - View All Parcels
    // ------------------------------------------------------------------

    // Handles the View All Parcels option
    private static void handleViewAll(ParcelManager manager) {
        System.out.println();
        System.out.println("---------------- VIEW ALL PARCELS ----------------");

        if (manager.isEmpty()) {
            System.out.println("No parcels found.");
            return;
        }

        printParcelTable(manager.getAllParcels());
    }

    // ------------------------------------------------------------------
    // 3 - Change Delivery Service
    // ------------------------------------------------------------------

    // Handles the Change Delivery Service option
    private static void handleChangeService(Scanner scanner, ParcelManager manager) {
        System.out.println();
        System.out.println("---------------- CHANGE DELIVERY SERVICE ----------------");

        System.out.print("Enter tracking number: ");
        String trackingNumber = scanner.nextLine().trim().toUpperCase();

        try {
            Parcel parcel = manager.findParcel(trackingNumber);
            if (parcel.isDelivered()) {
                throw new ParcelAlreadyDeliveredException("Parcel already delivered!");
            }
            DeliveryStrategy strategy = readService(scanner);
            manager.changeService(trackingNumber, strategy);
            System.out.println("Delivery service changed! New fee: "
                    + formatFee(parcel.getDeliveryFee()));
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException
                | SameDayNotAllowedException e) {
            System.out.println(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 4 - Mark as Delivered
    // ------------------------------------------------------------------

    // Handles the Mark as Delivered option
    private static void handleMarkDelivered(Scanner scanner, ParcelManager manager) {
        System.out.println();
        System.out.println("---------------- MARK AS DELIVERED ----------------");

        System.out.print("Enter tracking number: ");
        String trackingNumber = scanner.nextLine().trim().toUpperCase();

        try {
            manager.markDelivered(trackingNumber);
            System.out.println("Parcel delivered successfully!");
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException e) {
            System.out.println(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input helpers (all loop until valid input is given - never crash)
    // ------------------------------------------------------------------

    // Reads a menu choice from 1 to max and re-asks until it is valid
    private static int readMenuChoice(Scanner scanner, int max) {
        return readWholeNumber(scanner, "Enter your choice: ", 1, max,
                "Invalid choice! Please enter a number from 1 to " + max + ".");
    }

    // Reads a whole number from min to max, re-asking on anything else
    private static int readWholeNumber(Scanner scanner, String prompt, int min, int max,
                                       String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                if (!input.matches("\\d+")) {
                    throw new InvalidInputException(errorMessage);
                }
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    throw new InvalidInputException(errorMessage);
                }
                return value;
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(errorMessage);
            }
        }
    }

    // Reads a box dimension in cm (a whole number greater than 0)
    private static int readDimension(Scanner scanner, String name) {
        return readWholeNumber(scanner, "Enter " + name.toLowerCase() + " (cm): ", 1,
                Integer.MAX_VALUE, name + " must be a whole number greater than 0!");
    }

    // Reads a tracking number for a new parcel and checks the format and duplicates
    private static String readNewTrackingNumber(Scanner scanner, ParcelManager manager) {
        while (true) {
            System.out.print("Enter tracking number: ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                if (!input.matches("ABC\\d{4}")) {
                    throw new InvalidInputException("Invalid tracking number! "
                            + "Format must be ABC followed by 4 digits (e.g. ABC0001).");
                }
                if (manager.trackingExists(input)) {
                    throw new DuplicateTrackingNumberException(
                            "Tracking number already exists! Please use a different one.");
                }
                return input;
            } catch (InvalidInputException | DuplicateTrackingNumberException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Reads a text value that must not be empty or only spaces
    private static String readNonEmptyString(Scanner scanner, String prompt,
                                             String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                if (input.isEmpty()) {
                    throw new InvalidInputException(errorMessage);
                }
                return input;
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Reads a weight greater than 0 and not above the maximum for the parcel type
    private static double readWeight(Scanner scanner, double maxWeight, String tooHeavyMessage) {
        while (true) {
            System.out.print("Enter weight (kg): ");
            String input = scanner.nextLine().trim();
            try {
                if (!input.matches("\\d+(\\.\\d+)?")) {
                    throw new InvalidInputException("Weight must be a number greater than 0!");
                }
                double weight = Double.parseDouble(input);
                if (weight <= 0) {
                    throw new InvalidInputException("Weight must be greater than 0!");
                }
                if (weight > maxWeight) {
                    throw new InvalidInputException(tooHeavyMessage);
                }
                return weight;
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Shows the delivery service menu and returns the matching strategy object
    private static DeliveryStrategy readService(Scanner scanner) {
        System.out.println("Select delivery service:");
        System.out.println("1. Standard");
        System.out.println("2. Express");
        System.out.println("3. Same Day");
        int choice = readMenuChoice(scanner, 3);

        if (choice == 1) {
            return new StandardDelivery();
        } else if (choice == 2) {
            return new ExpressDelivery();
        } else {
            return new SameDayDelivery();
        }
    }

    // ------------------------------------------------------------------
    // Display helpers
    // ------------------------------------------------------------------

    // Prints all parcels in a formatted table
    private static void printParcelTable(List<Parcel> parcels) {
        String format = "%-14s%-10s%-17s%-24s%-11s%-16s%-10s%n";
        String separator = "=".repeat(TABLE_WIDTH);
        String divider = "-".repeat(TABLE_WIDTH);

        System.out.println();
        System.out.println(separator);
        System.out.println(centerText("ALL PARCELS", TABLE_WIDTH));
        System.out.println(separator);
        System.out.printf(format, "Tracking No.", "Type", "Receiver", "Chargeable Weight (kg)",
                "Service", "Delivery Fee", "Status");
        System.out.println(divider);
        for (Parcel parcel : parcels) {
            System.out.printf(format,
                    parcel.getTrackingNumber(),
                    parcel.getType(),
                    truncate(parcel.getReceiverName(), RECEIVER_DISPLAY_LENGTH),
                    parcel.getChargeableWeightText(),
                    parcel.getStrategy().getName(),
                    formatFee(parcel.getDeliveryFee()),
                    parcel.getStatus());
        }
        System.out.println(separator);
    }

    // Formats a fee as "Php 0.00"
    private static String formatFee(double fee) {
        return CURRENCY + String.format("%.2f", fee);
    }

    // Centers a title by adding spaces in front of it
    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    // Cuts a long text with "..." (used for the receiver name display only)
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
