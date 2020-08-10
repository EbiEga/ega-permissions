package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataServiceImpl;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.PermissionsServiceImpl;

@Configuration
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.permissions.persistence.repository"})
public class EgaPermissionsConfiguration {

    @Bean
    public PermissionsApiDelegate permissionsApiDelegate(final PermissionsService permissionsService) {
        return new PermissionsApiDelegateImpl(permissionsService);
    }

    @Bean
    public PermissionsService permissionsService(final PermissionsDataService permissionsDataService,
                                                 final TokenPayloadMapper tokenPayloadMapper,
                                                 final VisaInfoProperties visaInfoProperties) {
        return new PermissionsServiceImpl(permissionsDataService, tokenPayloadMapper, visaInfoProperties);
    }

    @Bean
    public PermissionsDataService permissionsDataService(final PassportClaimRepository passportClaimRepository) {
        return new PermissionsDataServiceImpl(passportClaimRepository);
    }

    @Bean
    @ConfigurationProperties(value = "ega-permissions.visainfo")
    public VisaInfoProperties visaInfoProperties() {
        return new VisaInfoProperties();
    }

}
