package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpMethod.*;

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
//        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
