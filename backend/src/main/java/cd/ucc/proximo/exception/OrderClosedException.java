package cd.ucc.proximo.exception;

public class OrderClosedException extends RuntimeException {
    public OrderClosedException(String message) {
        super(message);
    }
}
