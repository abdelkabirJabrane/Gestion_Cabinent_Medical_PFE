package ma.medicabpro.appointmentservice.exception;



import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind
        .MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleNotFound(
            AppointmentNotFoundException ex) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors =
                new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field =
                            ((FieldError) error).getField();
                    errors.put(field,
                            error.getDefaultMessage());
                });
        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now());
        body.put("status", 400);
        body.put("message",
                "Erreurs de validation");
        body.put("errors", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>>
    handleRuntime(RuntimeException ex) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>>
    buildError(HttpStatus status,
               String message) {
        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status)
                .body(body);
    }
}