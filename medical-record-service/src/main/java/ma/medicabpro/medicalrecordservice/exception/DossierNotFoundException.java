package ma.medicabpro.medicalrecordservice.exception;



public class DossierNotFoundException
        extends RuntimeException {
    public DossierNotFoundException(String message) {
        super(message);
    }
}
