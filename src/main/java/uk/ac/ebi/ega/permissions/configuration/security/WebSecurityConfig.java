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
package uk.ac.ebi.ega.permissions.configuration.security;

import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import uk.ac.ebi.ega.permissions.configuration.apikey.ApiKeyAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
    private AccessDeniedHandler accessDeniedHandler;
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public WebSecurityConfig(AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver,
                             AccessDeniedHandler accessDeniedHandler,
                             ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.authenticationManagerResolver = authenticationManagerResolver;
        this.accessDeniedHandler = accessDeniedHandler;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    // @formatter:off
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .addFilterBefore(apiKeyAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .authorizeRequests((authorizeRequests) ->
                        authorizeRequests
                                .antMatchers(swaggerEndpointMatcher())
                                .permitAll()
                                .anyRequest().authenticated())
                .csrf()
                .disable()
                .oauth2ResourceServer(configurer -> configurer.authenticationManagerResolver(this.authenticationManagerResolver))
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler);
    }

    private String[] swaggerEndpointMatcher() {
        return new String[]{
                "/api-specs/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/swagger-resources/**",
                "/swagger-ui/**"};
    }
    // @formatter:on
}
