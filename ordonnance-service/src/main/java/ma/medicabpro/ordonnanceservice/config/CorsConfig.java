package ma.medicabpro.ordonnanceservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:4200", "http://localhost:30010")
                .allowedMethods("DELETE", "GET", "POST", "PATCH", "PUT")
                .allowedHeaders("Access-Control-Allow-Headers", "Access-Control-Allow-Origin",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers",
                        "Origin", "Cache-Control", "Content-Type", "Authorization", "Content-Disposition")
                .exposedHeaders("Access-Control-Allow-Headers", "Access-Control-Allow-Origin",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers",
                        "Origin", "Cache-Control", "Content-Type", "Authorization", "Content-Disposition")
                .allowCredentials(true).maxAge(3600);
    }
}
