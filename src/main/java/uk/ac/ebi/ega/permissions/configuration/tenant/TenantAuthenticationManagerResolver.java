package uk.ac.ebi.ega.permissions.configuration.tenant;

import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TenantAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantAuthenticationManagerResolver.class);

    private String egaJwtIssUri;
    private String egaJwtJwkSetUri;
    private String elixirJtwIssUri;

    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    private final Map<String, String> tenants = new HashMap<>();

    private final Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();

    public TenantAuthenticationManagerResolver(final String egaJwtIssUri, final String egaJwtJwkSetUri, final String elixirJtwIssUri) {
        this.egaJwtIssUri = egaJwtIssUri;
        this.egaJwtJwkSetUri = egaJwtJwkSetUri;
        this.elixirJtwIssUri = elixirJtwIssUri;

        this.tenants.put(egaJwtIssUri, egaJwtIssUri);
        this.tenants.put(elixirJtwIssUri, elixirJtwIssUri);
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        return this.authenticationManagers.computeIfAbsent(toTenant(request), this::fromTenant);
    }

    private String toTenant(HttpServletRequest request) {
        try {
            String token = this.resolver.resolve(request);
            return (String) JWTParser.parse(token).getJWTClaimsSet().getClaim("iss");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private AuthenticationManager fromTenant(String tenant) {
        AuthenticationManager manager;
        LOGGER.debug("Validating token for Tenant: {}", tenant);
        if (egaJwtIssUri.equals(tenant)) {
            //NimbusJTWDecoder used because EGA OpenID server doesn't have the /.well-known/ configuration endpoint enabled
            JwtDecoder decoder = new NimbusJwtDecoderJwkSupport(egaJwtJwkSetUri);
            AuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
            manager = provider::authenticate;
        } else {
            manager = Optional.ofNullable(this.tenants.get(tenant))
                    .map(JwtDecoders::fromOidcIssuerLocation)
                    .map(JwtAuthenticationProvider::new)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"))::authenticate;
        }

        return manager;
    }

}
