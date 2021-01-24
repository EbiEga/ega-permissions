package uk.ac.ebi.ega.permissions;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import uk.ac.ebi.ega.permissions.service.SecurityService;

import java.io.Serializable;
import java.util.Optional;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@TestConfiguration
@Profile("unsecuretest")
public class TestApplication {

    public static class DummyPermissionEvaluator implements PermissionEvaluator {
        @Override
        public boolean hasPermission(Authentication authentication, Object o, Object o1) {
            return true;
        }

        @Override
        public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
            return true;
        }
    }

    @Bean
    public PermissionEvaluator customPermissionEvaluator() {
        return new DummyPermissionEvaluator();
    }

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
