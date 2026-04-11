package ma.medicabpro.appointmentservice.feign;


import org.springframework.cloud.openfeign
        .FeignClient;
import org.springframework.web.bind.annotation
        .GetMapping;
import org.springframework.web.bind.annotation
        .PathVariable;
import java.util.Map;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/patients/{id}")
    Map<String, Object> getPatientById(
            @PathVariable Long id);
}
