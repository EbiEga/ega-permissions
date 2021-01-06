package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.ResourceUtils;
import uk.ac.ebi.ega.permissions.api.AccountIdApiDelegate;
import uk.ac.ebi.ega.permissions.api.DatasetsApiDelegate;
import uk.ac.ebi.ega.permissions.api.MeApiDelegate;
import uk.ac.ebi.ega.permissions.configuration.tenant.TenantAuthenticationManagerResolver;
import uk.ac.ebi.ega.permissions.controller.CustomAccessDeniedHandler;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.controller.delegate.AccountIdApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.DatasetsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.MeApiDelegateImpl;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTAlgorithm;
import uk.ac.ebi.ega.permissions.persistence.repository.*;
import uk.ac.ebi.ega.permissions.persistence.service.*;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.JWTServiceImpl;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.PermissionsServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.stream.Collectors;

@Configuration
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.permissions.persistence.repository"})
public class EgaPermissionsConfig {

    @Bean
    public MeApiDelegate meApiDelegate(final RequestHandler requestHandler) {
        return new MeApiDelegateImpl(requestHandler);
    }

    @Bean
    public AccountIdApiDelegate accountIdApiDelegate(final PermissionsService permissionsService,
                                                     final RequestHandler requestHandler) {
        return new AccountIdApiDelegateImpl(permissionsService, requestHandler);
    }

    @Bean
    public DatasetsApiDelegate datasetsApiDelegate(final PermissionsService permissionsService, final RequestHandler requestHandler) {
        return new DatasetsApiDelegateImpl(permissionsService, requestHandler);
    }

    @Bean
    public PermissionsService permissionsService(final PermissionsDataService permissionsDataService,
                                                 final EventDataService eventDataService,
                                                 final TokenPayloadMapper tokenPayloadMapper,
                                                 final VisaInfoProperties visaInfoProperties) {
        return new PermissionsServiceImpl(permissionsDataService, eventDataService, tokenPayloadMapper, visaInfoProperties);
    }

    @Bean
    public PermissionsDataService permissionsDataService(final PassportClaimRepository passportClaimRepository,
                                                         final AccountElixirIdRepository accountElixirIdRepository,
                                                         final AccountRepository accountRepository) {
        return new PermissionsDataServiceImpl(passportClaimRepository, accountRepository, accountElixirIdRepository);
    }

    @Bean
    public UserGroupDataService userGroupDataService(final UserGroupRepository userGroupRepository) {
        return new UserGroupDataServiceImpl(userGroupRepository);
    }

    @Bean
    public EventDataService userEventDataService(final EventRepository eventRepository) {
        return new EventDataServiceImpl(eventRepository);
    }

    @Bean
    public RequestHandler requestHandler(final PermissionsService permissionsService,
                                         final TokenPayloadMapper tokenPayloadMapper,
                                         final UserGroupDataService userGroupDataService,
                                         final JWTService jwtService) {
        return new RequestHandler(permissionsService, tokenPayloadMapper, userGroupDataService, jwtService);
    }

    @Bean
    @ConfigurationProperties(value = "ega-permissions.visainfo")
    public VisaInfoProperties visaInfoProperties() {
        return new VisaInfoProperties();
    }

    @Bean
    public JWTService jwtService(@Value("${jwks.keystore.path}") String jwksKeystorePath,
                                 @Value("${jwks.signer.default-key.id}") String defaultSignerKeyId,
                                 @Value("${ega.openid.jwt.jwk-set-uri}") String jwksURL) throws IOException, ParseException, URISyntaxException {
        final File keystoreFile = ResourceUtils.getFile(jwksKeystorePath);
        assertFileExistsAndReadable(keystoreFile, String.format("Keystore file %s should exists & must be readable", keystoreFile.toString()));

        final String jwks = Files.lines(keystoreFile.toPath()).collect(Collectors.joining());
        return new JWTServiceImpl(
                jwks,
                defaultSignerKeyId,
                JWTAlgorithm.RS256,
                new URL(jwksURL));
    }

    @Bean
    public AuthenticationManagerResolver authenticationManagerResolver(@Value("${ega.openid.jwt.issuer-uri}") String egaJwtIssUri,
                                                                       @Value("${ega.openid.jwt.jwk-set-uri}") String egaJwtJwkSetUri,
                                                                       @Value("${elixir.openid.jwt.issuer-uri}") String elixirJwtIssUri) {
        return new TenantAuthenticationManagerResolver(egaJwtIssUri, egaJwtJwkSetUri, elixirJwtIssUri);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    private void assertFileExistsAndReadable(final File file, final String message) throws FileSystemException {
        if (!file.canRead()) {
            throw new FileSystemException(message);
        }
    }
}
