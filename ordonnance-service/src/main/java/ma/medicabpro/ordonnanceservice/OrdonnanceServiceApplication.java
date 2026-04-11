package ma.medicabpro.ordonnanceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class OrdonnanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdonnanceServiceApplication.class, args);
    }

}
