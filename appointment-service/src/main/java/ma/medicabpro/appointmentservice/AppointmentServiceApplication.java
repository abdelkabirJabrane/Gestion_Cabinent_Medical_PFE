package ma.medicabpro.appointmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ma.medicabpro.appointmentservice.feign")
public class AppointmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(
                AppointmentServiceApplication.class, args);
    }
}
