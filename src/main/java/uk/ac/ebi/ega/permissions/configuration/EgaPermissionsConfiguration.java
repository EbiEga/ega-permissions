package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.util.ResourceUtils;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.configuration.tenant.TenantAuthenticationManagerResolver;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.controller.delegate.PermissionsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTAlgorithm;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataServiceImpl;
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
public class EgaPermissionsConfiguration {

    @Bean
    public PermissionsApiDelegate permissionsApiDelegate(final PermissionsService permissionsService,
                                                         final RequestHandler requestHandler) {
        return new PermissionsApiDelegateImpl(permissionsService, requestHandler);
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
    public RequestHandler requestHandler(final PermissionsService permissionsService, final TokenPayloadMapper tokenPayloadMapper) {
        return new RequestHandler(permissionsService, tokenPayloadMapper);
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

    private void assertFileExistsAndReadable(final File file, final String message) throws FileSystemException {
        if (!file.canRead()) {
            throw new FileSystemException(message);
        }
    }
}
