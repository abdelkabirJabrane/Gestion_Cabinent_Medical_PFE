package ma.medicabpro.patientservice.exception;

public class PatientNotFoundException
        extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}