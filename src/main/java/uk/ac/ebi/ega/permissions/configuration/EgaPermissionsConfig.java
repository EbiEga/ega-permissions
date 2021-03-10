/*
 *
 * Copyright 2020-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.ResourceUtils;
import uk.ac.ebi.ega.permissions.api.AccessGroupsApiDelegate;
import uk.ac.ebi.ega.permissions.api.ApiKeyApiDelegate;
import uk.ac.ebi.ega.permissions.api.DatasetsApiDelegate;
import uk.ac.ebi.ega.permissions.api.GroupUsersApiDelegate;
import uk.ac.ebi.ega.permissions.api.MeApiDelegate;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.configuration.apikey.ApiKeyAuthenticationFilter;
import uk.ac.ebi.ega.permissions.configuration.tenant.TenantAuthenticationManagerResolver;
import uk.ac.ebi.ega.permissions.controller.CustomAccessDeniedHandler;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.controller.delegate.AccessGroupsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.ApiKeyApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.DatasetsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.GroupUsersApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.MeApiDelegateImpl;
import uk.ac.ebi.ega.permissions.controller.delegate.PermissionsApiDelegateImpl;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.mapper.GroupUserMapper;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTAlgorithm;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountElixirIdRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.ApiKeyRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.EventRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;
import uk.ac.ebi.ega.permissions.persistence.service.EventDataService;
import uk.ac.ebi.ega.permissions.persistence.service.EventDataServiceImpl;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataServiceImpl;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataServiceImpl;
import uk.ac.ebi.ega.permissions.service.ApiKeyService;
import uk.ac.ebi.ega.permissions.service.ApiKeyServiceImpl;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.JWTServiceImpl;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.PermissionsServiceImpl;
import uk.ac.ebi.ega.permissions.service.SecurityService;
import uk.ac.ebi.ega.permissions.service.SecurityServiceImpl;
import uk.ac.ebi.ega.permissions.utils.EncryptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.stream.Collectors;

@Configuration
public class EgaPermissionsConfig {

    @Bean
    public MeApiDelegate meApiDelegate(final RequestHandler requestHandler) {
        return new MeApiDelegateImpl(requestHandler);
    }

    @Bean
    public PermissionsApiDelegate accountIdApiDelegate(final PermissionsService permissionsService,
                                                       final RequestHandler requestHandler) {
        return new PermissionsApiDelegateImpl(permissionsService, requestHandler);
    }

    @Bean
    public DatasetsApiDelegate datasetsApiDelegate(final PermissionsService permissionsService, final RequestHandler requestHandler) {
        return new DatasetsApiDelegateImpl(permissionsService, requestHandler);
    }

    @Bean
    public PermissionsService permissionsService(final PermissionsDataService permissionsDataService,
                                                 final EventDataService eventDataService,
                                                 final TokenPayloadMapper tokenPayloadMapper,
                                                 final VisaInfoProperties visaInfoProperties,
                                                 final SecurityService securityService) {
        return new PermissionsServiceImpl(permissionsDataService, eventDataService, tokenPayloadMapper, visaInfoProperties, securityService);
    }

    @Bean
    public PermissionsDataService permissionsDataService(final PassportClaimRepository passportClaimRepository,
                                                         final AccountElixirIdRepository accountElixirIdRepository,
                                                         final AccountRepository accountRepository,
                                                         final UserGroupRepository userGroupRepository) {
        return new PermissionsDataServiceImpl(passportClaimRepository, accountRepository, accountElixirIdRepository, userGroupRepository);
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
                                         final JWTService jwtService,
                                         final SecurityService securityService) {
        return new RequestHandler(permissionsService, tokenPayloadMapper, userGroupDataService, jwtService, securityService);
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

    @Bean
    public EncryptionUtils encryptionUtils(@Value("${apiKey.user-algorithm}") String userAlgorithm,
                                           @Value("${apiKey.ega-algorithm}") String egaAlgorithm) {
        return new EncryptionUtils(userAlgorithm, egaAlgorithm);

    }

    @Bean
    public PermissionEvaluator permissionEvaluator(final PermissionsService permissionsService,
                                                   final UserGroupDataService userGroupDataService) {
        return new CustomPermissionEvaluator(permissionsService, userGroupDataService);
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityServiceImpl();
    }

    @Bean
    public ApiKeyService apiKeyService(@Value("${apiKey.ega-password}") String egaPassword,
                                       final ApiKeyMapper apiKeyMapper,
                                       final ApiKeyRepository apiKeyRepository,
                                       final EncryptionUtils encryptionUtils) {
        return new ApiKeyServiceImpl(egaPassword, apiKeyMapper, apiKeyRepository, encryptionUtils);
    }

    @Bean
    public ApiKeyApiDelegate apikeyApiDelegate(final ApiKeyService apiKeyService) {
        return new ApiKeyApiDelegateImpl(apiKeyService);
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(final ApiKeyService apiKeyService) {
        return new ApiKeyAuthenticationFilter(apiKeyService);
    }

    @Bean
    public AccessGroupsApiDelegate accessGroupsApiDelegate(final UserGroupDataService userGroupDataService,
                                                           final GroupUserMapper groupUserMapper) {
        return new AccessGroupsApiDelegateImpl(userGroupDataService, groupUserMapper);
    }

    @Bean
    public GroupUsersApiDelegate groupUsersApiDelegate() {
        return new GroupUsersApiDelegateImpl();
    }

    private void assertFileExistsAndReadable(final File file, final String message) throws FileSystemException {
        if (!file.canRead()) {
            throw new FileSystemException(message);
        }
    }
}
