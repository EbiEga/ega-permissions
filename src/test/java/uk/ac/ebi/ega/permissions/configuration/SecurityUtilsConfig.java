package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.permissions.service.SecurityService;

import java.util.Optional;

@Configuration
public class SecurityUtilsConfig {

    @Bean
    public SecurityService securityService() {
        return new SecurityService() {
            @Override
            public Optional<String> getCurrentUser() {
                return Optional.of("test@ebi.ac.uk");
            }
        };
    }
}
