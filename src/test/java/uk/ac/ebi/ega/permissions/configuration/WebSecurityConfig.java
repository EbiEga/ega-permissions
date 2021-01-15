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

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
    private AccessDeniedHandler accessDeniedHandler;

    public WebSecurityConfig(AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver,
                             AccessDeniedHandler accessDeniedHandler) {
        this.authenticationManagerResolver = authenticationManagerResolver;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("test@ebi.ac.uk")
                .password("secret")
                .authorities("DAC_write", "EGAAdmin_read")
                .roles("DAC_write", "EGAAdmin_read");
    }


    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests((authorizeRequests) ->
                        authorizeRequests
                                .antMatchers(GET, "/datasets/{datasetId}/**")
                                .access("hasPermission(#datasetId, 'DAC_read')")
                                .antMatchers(GET, "/{accountId}/**")
                                .access("hasPermission(#accountId, 'EGAAdmin_read')")
                                .antMatchers(POST, "/{accountId}/**")
                                .access("hasPermission(#accountId, 'DAC_write')")
                                .antMatchers(DELETE, "/{accountId}/**")
                                .access("hasPermission(#accountId, 'DAC_write')")
                                .antMatchers(swaggerEndpointMatcher())
                                .permitAll()
                                .anyRequest().authenticated())
                .csrf()
                .disable()
                .oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver)).exceptionHandling().accessDeniedHandler(accessDeniedHandler);
    }

    private String[] swaggerEndpointMatcher() {
        return new String[]{
                "/api-specs/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/swagger-resources/**",
                "/swagger-ui/**"};
    }


}
