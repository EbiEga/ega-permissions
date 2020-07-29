package uk.ac.ebi.ega.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataServiceDummy;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.PermissionsServiceImpl;

@Configuration
public class EgaPermissionsConfiguration {

    private final TokenPayloadMapper tokenPayloadMapper;

    @Autowired
    public EgaPermissionsConfiguration(TokenPayloadMapper tokenPayloadMapper) {
        this.tokenPayloadMapper = tokenPayloadMapper;
    }

    @Bean
    public PermissionsDataService permissionsDataService() {
        return new PermissionsDataServiceDummy();
    }

    @Bean
    public PermissionsService permissionsService() {
        return new PermissionsServiceImpl(permissionsDataService(), tokenPayloadMapper);
    }

    @Bean
    public PermissionsApiDelegate permissionsApiDelegate(){
        return new PermissionsApiDelegateImpl(permissionsService());
    }

}
