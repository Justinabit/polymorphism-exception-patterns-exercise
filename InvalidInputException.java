// Thrown when the user enters invalid input
public class InvalidInputException extends Exception {

    private static final long serialVersionUID = 1L;

    // Creates the exception with a message
    public InvalidInputException(String message) {
        super(message);
    }
}
