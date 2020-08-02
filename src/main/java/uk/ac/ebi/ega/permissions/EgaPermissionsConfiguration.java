package uk.ac.ebi.ega.permissions;

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
@EnableJpaRepositories
public class EgaPermissionsConfiguration {

    private final TokenPayloadMapper tokenPayloadMapper;

    private final PassportClaimRepository passportClaimRepository;

    public EgaPermissionsConfiguration(TokenPayloadMapper tokenPayloadMapper, PassportClaimRepository passportClaimRepository){
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.passportClaimRepository = passportClaimRepository;
    }

    @Bean
    public PermissionsService permissionsService() {
        return new PermissionsServiceImpl(permissionsDataService(), tokenPayloadMapper);
    }

    @Bean
    public PermissionsApiDelegate permissionsApiDelegate() {
        return new PermissionsApiDelegateImpl(permissionsService());
    }

    @Bean
    public PermissionsDataService permissionsDataService() {
        return new PermissionsDataServiceImpl(passportClaimRepository);
    }

}
