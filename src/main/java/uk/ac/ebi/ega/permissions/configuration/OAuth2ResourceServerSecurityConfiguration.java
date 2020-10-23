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

import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;

@EnableWebSecurity
public class OAuth2ResourceServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    public OAuth2ResourceServerSecurityConfiguration(AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver){
        this.authenticationManagerResolver = authenticationManagerResolver;
    }

    // @formatter:off
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests((authorizeRequests) ->
                authorizeRequests
                    .antMatchers(swaggerEndpointMatcher())
                    .permitAll()
                    .anyRequest().authenticated())
            .csrf()
            .disable()
            .oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver));
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
