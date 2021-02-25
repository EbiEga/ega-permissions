package uk.ac.ebi.ega.permissions.helpers;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.dto.TokenParams;

import java.net.URI;
import java.net.URISyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD;

public class AccessTokenHelper {

    private RestTemplate restTemplate;
    private TokenParams tokenParams;

    public AccessTokenHelper(RestTemplate restTemplate, TokenParams tokenParams) {
        this.restTemplate = restTemplate;
        this.tokenParams = tokenParams;
    }

    public String obtainAccessTokenFromEGA() throws URISyntaxException {
        String clientId = this.tokenParams.getClientId();
        String clientSecret = this.tokenParams.getClientSecret();
        String authURL = this.tokenParams.getAuthURL();
        String username = this.tokenParams.getUsername();
        String password = this.tokenParams.getPassword();

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", PASSWORD.getValue());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "openid");
        params.add("username", username);
        params.add("password", password);

        final HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI(authURL))
                .path("/token")
                .build()
                .toUri();

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, tokenHeaders);
        final ResponseEntity<String> response = restTemplate.postForEntity(requestURI, request, String.class);

        final JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(response.getBody()).get("access_token").toString();
    }

    public HttpHeaders getAccessTokenHeader(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, "Bearer " + accessToken);
        return headers;
    }

    private String httpBasicAuth(final String clientId, final String clientSecret) {
        final byte[] toEncode = (clientId + ":" + clientSecret).getBytes(UTF_8);
        return "Basic " + new String(getEncoder().encode(toEncode));
    }
}
