package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.permissions.service.SecurityService;
import uk.ac.ebi.ega.permissions.service.SecurityServiceImpl;

@Configuration
public class SecurityUtilsConfig {
    @Bean
    public SecurityService securityService() {
        return new SecurityServiceImpl();
    }
}
