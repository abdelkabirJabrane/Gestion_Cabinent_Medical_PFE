package ma.medicabpro.billingservice.exception;



public class FactureNotFoundException
        extends RuntimeException {
    public FactureNotFoundException(
            String message) {
        super(message);
    }
}
