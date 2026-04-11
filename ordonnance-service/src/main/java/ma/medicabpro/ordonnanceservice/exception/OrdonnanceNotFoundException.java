package ma.medicabpro.ordonnanceservice.exception;

public class OrdonnanceNotFoundException extends RuntimeException {
    public OrdonnanceNotFoundException(String message) {
        super(message);
    }
}
