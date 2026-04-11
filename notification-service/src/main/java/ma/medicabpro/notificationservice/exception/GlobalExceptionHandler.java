package ma.medicabpro.notificationservice.exception;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            NotificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleNotFound(
            NotificationNotFoundException ex) {
        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now());
        body.put("status", 404);
        body.put("message", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>>
    handleRuntime(RuntimeException ex) {
        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now());
        body.put("status", 400);
        body.put("message", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}