import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Main class: shows the menu and handles all user input
public class Main {

    private static final int RECEIVER_MAX_LENGTH = 50;
    private static final int RECEIVER_DISPLAY_LENGTH = 16;

    // Starts the program and keeps showing the menu until the user exits
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ParcelManager manager = ParcelManager.getInstance();

        boolean running = true;
        try {
            while (running) {
                printMainMenu(manager);
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
                        break;
                    default:
                        // readMenuChoice guarantees this is unreachable.
                        break;
                }

                if (running) {
                    waitForEnter(scanner);
                }
            }
        } catch (NoSuchElementException e) {
            // The input stream ended (Ctrl+D), so exit gracefully.
        }

        printGoodbye();
        scanner.close();
    }

    // ------------------------------------------------------------------
    // Menu display
    // ------------------------------------------------------------------

    // Prints the main menu and a quick count of the parcels
    private static void printMainMenu(ParcelManager manager) {
        int total = manager.getAllParcels().size();
        int pending = manager.getPendingParcels().size();
        int delivered = total - pending;

        System.out.println();
        System.out.println("+==========================================================+");
        System.out.println("|                  PARCEL DELIVERY SYSTEM                  |");
        System.out.println("+==========================================================+");
        System.out.println("|   [1] Add Parcel                                         |");
        System.out.println("|   [2] View All Parcels                                   |");
        System.out.println("|   [3] Change Delivery Service                            |");
        System.out.println("|   [4] Mark as Delivered                                  |");
        System.out.println("|   [5] Exit                                               |");
        System.out.println("+----------------------------------------------------------+");
        System.out.printf("|   Parcels: %-4d    Pending: %-4d    Delivered: %-4d      |%n",
                total, pending, delivered);
        System.out.println("+==========================================================+");
    }

    // Prints the goodbye message
    private static void printGoodbye() {
        System.out.println();
        System.out.println();
        System.out.println("+==========================================================+");
        System.out.println("|     Thank you for using the Parcel Delivery System!      |");
        System.out.println("|                         Goodbye!                         |");
        System.out.println("+==========================================================+");
    }

    // Waits for Enter so the user can read the result before the menu comes back
    private static void waitForEnter(Scanner scanner) {
        System.out.println();
        System.out.print("  Press Enter to go back to the main menu...");
        scanner.nextLine();
    }

    // ------------------------------------------------------------------
    // 1 - Add Parcel
    // ------------------------------------------------------------------

    // Handles the Add Parcel option
    private static void handleAddParcel(Scanner scanner, ParcelManager manager) {
        ConsoleUI.printSection("ADD PARCEL");

        ConsoleUI.printSubheading("Parcel Type");
        System.out.println("    [1] Document   flat Php 50.00, max 0.5 kg");
        System.out.println("    [2] Box        Php 40.00 per kg, max 50 kg");
        System.out.println("    [3] Fragile    Box rate + Php 100.00 handling fee, max 30 kg");
        int type = readMenuChoice(scanner, 3);

        ConsoleUI.printSubheading("Parcel Details");
        String trackingNumber = readNewTrackingNumber(scanner, manager);
        String receiver = readReceiverName(scanner);

        Parcel parcel;
        if (type == 1) {
            double weight = readWeight(scanner, Document.MAX_WEIGHT,
                    "Document weight must not exceed 0.5 kg!");
            int pages = readWholeNumber(scanner, "Enter number of pages (1-100)",
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
            String note = readText(scanner, "Handling note", Fragile.MAX_NOTE_LENGTH);
            parcel = new Fragile(trackingNumber, receiver, weight, new StandardDelivery(),
                    length, width, height, note);
        }

        // Keep asking for the service until a valid one is chosen
        while (true) {
            DeliveryStrategy strategy = readService(scanner, parcel);
            try {
                parcel.setStrategy(strategy);
                manager.addParcel(parcel);
                printParcelSummary(parcel);
                System.out.println();
                ConsoleUI.printSuccess("Parcel added successfully! Delivery fee: "
                        + ConsoleUI.formatFee(parcel.getDeliveryFee()));
                return;
            } catch (SameDayNotAllowedException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (DuplicateTrackingNumberException e) {
                ConsoleUI.printError(e.getMessage());
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // 2 - View All Parcels
    // ------------------------------------------------------------------

    // Handles the View All Parcels option
    private static void handleViewAll(ParcelManager manager) {
        ConsoleUI.printSection("VIEW ALL PARCELS");

        if (manager.isEmpty()) {
            ConsoleUI.printInfo("No parcels found. Choose [1] Add Parcel first.");
            return;
        }

        List<Parcel> parcels = manager.getAllParcels();
        printParcelTable(parcels);
        printTotals(parcels);
    }

    // ------------------------------------------------------------------
    // 3 - Change Delivery Service
    // ------------------------------------------------------------------

    // Handles the Change Delivery Service option
    private static void handleChangeService(Scanner scanner, ParcelManager manager) {
        ConsoleUI.printSection("CHANGE DELIVERY SERVICE");

        if (!showPendingParcels(manager)) {
            return;
        }
        String trackingNumber = readTrackingNumberToFind(scanner);
        if (trackingNumber.isEmpty()) {
            ConsoleUI.printInfo("Cancelled. No changes were made.");
            return;
        }

        try {
            Parcel parcel = manager.findPendingParcel(trackingNumber);
            String oldService = parcel.getStrategy().getName();
            String oldFee = ConsoleUI.formatFee(parcel.getDeliveryFee());
            System.out.println();
            ConsoleUI.printField("Parcel", trackingNumber + " - " + parcel.getType()
                    + " for " + parcel.getReceiverName());
            ConsoleUI.printField("Current service", oldService + " - " + oldFee);

            DeliveryStrategy strategy = readService(scanner, parcel);
            if (strategy.getName().equals(oldService)) {
                ConsoleUI.printInfo("Parcel " + trackingNumber + " already uses " + oldService
                        + ". No changes were made.");
                return;
            }

            manager.changeService(trackingNumber, strategy);
            System.out.println();
            ConsoleUI.printSuccess("Delivery service changed!");
            ConsoleUI.printField("Before", oldService + " - " + oldFee);
            ConsoleUI.printField("After", strategy.getName() + " - "
                    + ConsoleUI.formatFee(parcel.getDeliveryFee()));
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException
                | SameDayNotAllowedException e) {
            ConsoleUI.printError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 4 - Mark as Delivered
    // ------------------------------------------------------------------

    // Handles the Mark as Delivered option
    private static void handleMarkDelivered(Scanner scanner, ParcelManager manager) {
        ConsoleUI.printSection("MARK AS DELIVERED");

        if (!showPendingParcels(manager)) {
            return;
        }
        String trackingNumber = readTrackingNumberToFind(scanner);
        if (trackingNumber.isEmpty()) {
            ConsoleUI.printInfo("Cancelled. No changes were made.");
            return;
        }

        try {
            Parcel parcel = manager.findPendingParcel(trackingNumber);
            System.out.println();
            ConsoleUI.printField("Parcel", trackingNumber + " - " + parcel.getType()
                    + " for " + parcel.getReceiverName());
            if (!readYesNo(scanner, "Mark this parcel as delivered? This cannot be undone")) {
                ConsoleUI.printInfo("Cancelled. Parcel " + trackingNumber + " is still Pending.");
                return;
            }
            manager.markDelivered(trackingNumber);
            ConsoleUI.printSuccess("Parcel " + trackingNumber + " is now marked as Delivered!");
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException e) {
            ConsoleUI.printError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input helpers (all loop until valid input is given - never crash)
    // ------------------------------------------------------------------

    // Reads a menu choice from 1 to max and re-asks until it is valid
    private static int readMenuChoice(Scanner scanner, int max) {
        System.out.println();
        return readWholeNumber(scanner, "Enter your choice (1-" + max + ")", 1, max,
                "Invalid choice! Please enter a number from 1 to " + max + ".");
    }

    // Reads a whole number from min to max, re-asking on anything else
    private static int readWholeNumber(Scanner scanner, String prompt, int min, int max,
                                       String errorMessage) {
        while (true) {
            ConsoleUI.prompt(prompt);
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
                ConsoleUI.printError(e.getMessage());
            } catch (NumberFormatException e) {
                // The number has too many digits to fit in an int
                ConsoleUI.printError(errorMessage);
            }
        }
    }

    // Reads a box dimension in cm (a whole number from 1 to the maximum size)
    private static int readDimension(Scanner scanner, String name) {
        return readWholeNumber(scanner,
                "Enter " + name.toLowerCase() + " in cm (1-" + Box.MAX_DIMENSION + ")",
                1, Box.MAX_DIMENSION,
                name + " must be a whole number from 1 to " + Box.MAX_DIMENSION + "!");
    }

    // Reads a tracking number for a new parcel and checks the format and duplicates
    private static String readNewTrackingNumber(Scanner scanner, ParcelManager manager) {
        while (true) {
            ConsoleUI.prompt("Enter tracking number (e.g. ABC0001)");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                checkTrackingFormat(input);
                manager.checkNewTrackingNumber(input);
                return input;
            } catch (InvalidInputException | DuplicateTrackingNumberException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Reads the tracking number of an existing parcel; returns "" if the user just presses Enter
    private static String readTrackingNumberToFind(Scanner scanner) {
        System.out.println();
        while (true) {
            ConsoleUI.prompt("Enter tracking number (or press Enter to go back)");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                return input;
            }
            try {
                checkTrackingFormat(input);
                return input;
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Checks that a tracking number is ABC followed by 4 digits
    private static void checkTrackingFormat(String trackingNumber) throws InvalidInputException {
        if (!trackingNumber.matches("ABC\\d{4}")) {
            throw new InvalidInputException("Invalid tracking number! "
                    + "Format must be ABC followed by 4 digits (e.g. ABC0001).");
        }
    }

    // Reads text that is not empty and not longer than maxLength
    private static String readText(Scanner scanner, String fieldName, int maxLength) {
        while (true) {
            ConsoleUI.prompt("Enter " + fieldName.toLowerCase());
            String input = scanner.nextLine().trim();
            try {
                if (input.isEmpty()) {
                    throw new InvalidInputException(fieldName + " cannot be empty!");
                }
                if (input.length() > maxLength) {
                    throw new InvalidInputException(fieldName + " must be at most "
                            + maxLength + " characters!");
                }
                return input;
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Reads the receiver's name and makes sure it looks like a real name
    private static String readReceiverName(Scanner scanner) {
        while (true) {
            String name = readText(scanner, "Receiver name", RECEIVER_MAX_LENGTH);
            try {
                if (!isValidName(name)) {
                    throw new InvalidInputException("Receiver name must start with a letter and "
                            + "use only letters, spaces, periods, hyphens or apostrophes!");
                }
                return name;
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Checks that a name starts with a letter and has only letters, spaces, . - or '
    private static boolean isValidName(String name) {
        if (!Character.isLetter(name.charAt(0))) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean allowed = Character.isLetter(c) || c == ' ' || c == '.'
                    || c == '-' || c == '\'';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }

    // Reads a weight greater than 0 (up to 2 decimals) and not above the maximum
    private static double readWeight(Scanner scanner, double maxWeight, String tooHeavyMessage) {
        while (true) {
            ConsoleUI.prompt("Enter weight (kg)");
            String input = scanner.nextLine().trim();
            try {
                if (!input.matches("\\d+(\\.\\d{1,2})?")) {
                    throw new InvalidInputException("Weight must be a positive number "
                            + "with up to 2 decimal places (e.g. 2 or 0.25)!");
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
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Asks a yes/no question and re-asks until the answer is Y or N
    private static boolean readYesNo(Scanner scanner, String question) {
        while (true) {
            ConsoleUI.prompt(question + " (Y/N)");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                if (input.equals("Y")) {
                    return true;
                }
                if (input.equals("N")) {
                    return false;
                }
                throw new InvalidInputException("Please type Y for yes or N for no.");
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Shows every delivery service with its fee for this parcel and returns the chosen one
    private static DeliveryStrategy readService(Scanner scanner, Parcel parcel) {
        DeliveryStrategy[] services = {
            new StandardDelivery(), new ExpressDelivery(), new SameDayDelivery()
        };

        ConsoleUI.printSubheading("Delivery Service");
        ConsoleUI.printField("Chargeable weight", parcel.getChargeableWeightText() + " kg");
        ConsoleUI.printField("Details", parcel.getDetails());
        System.out.println();

        // Polymorphism: each service calculates its own fee and decides if it is allowed
        for (int i = 0; i < services.length; i++) {
            DeliveryStrategy service = services[i];
            String fee;
            if (service.isAllowed(parcel)) {
                fee = ConsoleUI.formatFee(service.calculateFee(parcel));
            } else {
                fee = "Not available for this parcel";
            }
            System.out.printf("    [%d] %-10s %s%n", i + 1, service.getName(), fee);
        }

        int choice = readMenuChoice(scanner, services.length);
        return services[choice - 1];
    }

    // ------------------------------------------------------------------
    // Display helpers
    // ------------------------------------------------------------------

    // Shows the parcels that can still be updated; returns false if there are none
    private static boolean showPendingParcels(ParcelManager manager) {
        if (manager.isEmpty()) {
            ConsoleUI.printInfo("No parcels found. Choose [1] Add Parcel first.");
            return false;
        }
        List<Parcel> pending = manager.getPendingParcels();
        if (pending.isEmpty()) {
            ConsoleUI.printInfo("All parcels are already delivered. There is nothing to update.");
            return false;
        }
        System.out.println();
        System.out.println("  Pending parcels:");
        printParcelTable(pending);
        return true;
    }

    // Prints parcels in a table; the details of each parcel go on the line below it
    private static void printParcelTable(List<Parcel> parcels) {
        String format = "  %-14s%-10s%-18s%13s   %-10s%14s   %s%n";
        String line = "  " + "-".repeat(95);

        System.out.println(line);
        System.out.printf(format, "Tracking No.", "Type", "Receiver", "Chargeable Wt",
                "Service", "Delivery Fee", "Status");
        System.out.println(line);
        for (Parcel parcel : parcels) {
            System.out.printf(format,
                    parcel.getTrackingNumber(),
                    parcel.getType(),
                    ConsoleUI.truncate(parcel.getReceiverName(), RECEIVER_DISPLAY_LENGTH),
                    parcel.getChargeableWeightText() + " kg",
                    parcel.getStrategy().getName(),
                    ConsoleUI.formatFee(parcel.getDeliveryFee()),
                    parcel.getStatus());
            System.out.println("                Details: " + parcel.getDetails());
        }
        System.out.println(line);
    }

    // Prints how many parcels there are and the total of all delivery fees
    private static void printTotals(List<Parcel> parcels) {
        int delivered = 0;
        double totalFees = 0;
        for (Parcel parcel : parcels) {
            if (parcel.isDelivered()) {
                delivered++;
            }
            totalFees += parcel.getDeliveryFee();
        }
        int pending = parcels.size() - delivered;

        ConsoleUI.printField("Total parcels", String.valueOf(parcels.size()));
        ConsoleUI.printField("Pending", String.valueOf(pending));
        ConsoleUI.printField("Delivered", String.valueOf(delivered));
        ConsoleUI.printField("Total fees", ConsoleUI.formatFee(totalFees));
    }

    // Prints all the information of one parcel after it is added
    private static void printParcelSummary(Parcel parcel) {
        ConsoleUI.printSubheading("Parcel Summary");
        ConsoleUI.printField("Tracking No.", parcel.getTrackingNumber());
        ConsoleUI.printField("Type", parcel.getType());
        ConsoleUI.printField("Receiver", parcel.getReceiverName());
        ConsoleUI.printField("Details", parcel.getDetails());
        ConsoleUI.printField("Actual weight", String.format("%.2f kg", parcel.getWeight()));
        ConsoleUI.printField("Chargeable weight", parcel.getChargeableWeightText() + " kg");
        ConsoleUI.printField("Base fee", ConsoleUI.formatFee(parcel.getBaseFee()));
        ConsoleUI.printField("Service", parcel.getStrategy().getName());
        ConsoleUI.printField("Delivery fee", ConsoleUI.formatFee(parcel.getDeliveryFee()));
        ConsoleUI.printField("Status", parcel.getStatus().toString());
    }
}
