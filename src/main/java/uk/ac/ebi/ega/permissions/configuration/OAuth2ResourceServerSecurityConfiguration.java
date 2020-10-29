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
package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class OAuth2ResourceServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // @formatter:off
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests((authorizeRequests) ->
                authorizeRequests
                    .antMatchers(GET, "/plain/{accountId}/**", "/jwt/{accountId}/**")
                        .access("hasPermission(#accountId, 'EGAAdmin_read')")
                    .antMatchers(GET, "/plain/datasets/{datasetId}/**", "/jwt/datasets/{datasetId}/**")
                        .access("hasPermission(#datasetId, 'EGAAdmin_read')")
                    .antMatchers(POST, "/plain/{accountId}/**", "/jwt/{accountId}/**")
                        .access("hasPermission(#accountId, 'DAC_write')")
                    .antMatchers(DELETE, "/plain/{accountId}/**", "/jwt/{accountId}/**")
                        .access("hasPermission(#accountId, 'DAC_write')")
                    .antMatchers(swaggerEndpointMatcher())
                    .permitAll()
                    .anyRequest().authenticated())
            .csrf()
            .disable()
            .oauth2ResourceServer((oauth2ResourceServer) ->
                oauth2ResourceServer
                    .jwt(withDefaults())
            );
    }

    private String[] swaggerEndpointMatcher() {
        return new String[]{
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"};
    }
    // @formatter:on
}
