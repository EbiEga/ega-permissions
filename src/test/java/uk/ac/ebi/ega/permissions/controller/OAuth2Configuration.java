/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.crypto.keys.KeyManager;
import org.springframework.security.crypto.keys.StaticKeyGeneratingKeyManager;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.server.authorization.web.JwkSetEndpointFilter.DEFAULT_JWK_SET_ENDPOINT_URI;
import static org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter.DEFAULT_AUTHORIZATION_ENDPOINT_URI;
import static org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter.DEFAULT_TOKEN_ENDPOINT_URI;

//@EnableWebSecurity
public class OAuth2Configuration extends WebSecurityConfigurerAdapter {

    // @formatter:off
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers(authorizationEndpointMatcher())
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(tokenEndpointMatcher()))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .apply(new OAuth2AuthorizationServerConfigurer<>());
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(@Value("${spring.security.oauth2.authorizationserver.client-id}") final String clientId,
                                                                 @Value("${spring.security.oauth2.authorizationserver.client-secret}") final String clientSecret,
                                                                 @Value("${spring.security.oauth2.authorizationserver.scope}") final String scope) {
        final RegisteredClient registeredClient = RegisteredClient
                .withId(randomUUID().toString())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(BASIC)
                .authorizationGrantType(CLIENT_CREDENTIALS)
                .scope(scope)
                .build();
        return new InMemoryRegisteredClientRepository(registeredClient);
    }
    // @formatter:on

    @Bean
    public KeyManager keyManager() {
        return new StaticKeyGeneratingKeyManager();
    }

    private static String[] authorizationEndpointMatcher() {
        return new String[]{
                DEFAULT_AUTHORIZATION_ENDPOINT_URI,
                DEFAULT_TOKEN_ENDPOINT_URI,
                DEFAULT_JWK_SET_ENDPOINT_URI
        };
    }

    private static RequestMatcher tokenEndpointMatcher() {
        return new AntPathRequestMatcher(
                DEFAULT_TOKEN_ENDPOINT_URI,
                POST.name()
        );
    }
}
