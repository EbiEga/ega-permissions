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

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static uk.ac.ebi.ega.permissions.controller.RequestHandler.EGA_ACCOUNT_ID_PREFIX;

@TestPropertySource("classpath:application-test.properties")
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {EgaPermissionsApplicationTest.class, SecurityAutoConfiguration.class},
        webEnvironment = DEFINED_PORT)
public class PermissionsControllerTest {

    private static final String ELIXIR_ACCOUNT_ID = "test@elixir-europe.org";
    private static final String EGA_ACCOUNT_ID = "EGAW00000000001";

    @Value("${spring.security.oauth2.authorizationserver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.authorizationserver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.authorizationserver.scope}")
    private String scope;

    @Value("${application.test.url}")
    private String applicationTestURL;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private RequestHandler requestHandler;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getJWTPermissions_WhenPassEGAAccountIdWithAccessToken_ReturnsSuccess() throws URISyntaxException {
        commonMock();
        final String accessToken = obtainAccessToken();
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getAccessTokenHeader(accessToken));

        assertStatus(restTemplate
                .exchange(getPermissionsURI(EGA_ACCOUNT_ID), GET, httpEntity, String.class), OK);
    }

    @Test
    public void getJWTPermissions_WhenPassEGAElixirAccountIdWithAccessToken_ReturnsSuccess() throws URISyntaxException {
        commonMock();
        when(requestHandler.getAccountIdForElixirId(ELIXIR_ACCOUNT_ID)).thenReturn(EGA_ACCOUNT_ID_PREFIX);
        final String accessToken = obtainAccessToken();
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getAccessTokenHeader(accessToken));

        assertStatus(restTemplate.exchange(getPermissionsURI(ELIXIR_ACCOUNT_ID), GET, httpEntity, String.class), OK);
    }

    @Test
    public void getJWTPermissions_WhenPassElixirAccountIdNoMappingWithEGAAccountId_FailsWithNotFound() throws URISyntaxException {
        commonMock();
        when(requestHandler.getAccountIdForElixirId(ELIXIR_ACCOUNT_ID)).thenThrow(ValidationException.class);
        final String accessToken = obtainAccessToken();
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getAccessTokenHeader(accessToken));

        assertStatus(restTemplate.exchange(getPermissionsURI(ELIXIR_ACCOUNT_ID), GET, httpEntity, String.class), NOT_FOUND);
    }

    @Test
    public void getJWTPermissions_WhenPassEGAAccountIdWithoutAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        assertStatus(restTemplate.getForEntity(getPermissionsURI(EGA_ACCOUNT_ID), String.class), UNAUTHORIZED);
    }

    @Test
    public void getJWTPermissions_WhenPassEGAAccountIdWithInvalidAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getAccessTokenHeader(getInvalidAccessToken()));

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), GET, httpEntity, String.class), UNAUTHORIZED);
    }

    @Test
    public void createPermissions_WhenPassEGAAccountIdAndPermissionsWithAccessToken_ReturnsSuccess() throws URISyntaxException {
        when(requestHandler.createJWTPermissions(startsWith(EGA_ACCOUNT_ID_PREFIX), anyList()))
                .thenReturn(emptyList());

        final String accessToken = obtainAccessToken();
        final HttpEntity<List<String>> httpEntity = new HttpEntity<>(emptyList(), getAccessTokenHeader(accessToken));

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), POST, httpEntity, String.class), MULTI_STATUS);
    }

    @Test
    public void createJWTPermissions_WhenPassEGAAccountIdAndPermissionsWithoutAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        final HttpEntity<List<String>> httpEntity = new HttpEntity<>(emptyList());

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), POST, httpEntity, String.class), UNAUTHORIZED);
    }

    @Test
    public void createJWTPermissions_WhenPassEGAAccountIdAndPermissionsWithInvalidAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        final HttpEntity<List<String>> httpEntity = new HttpEntity<>(emptyList(), getAccessTokenHeader(getInvalidAccessToken()));

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), POST, httpEntity, String.class), UNAUTHORIZED);
    }

    @Test
    public void deletePermissions_WhenPassEGAAccountIdWithAccessToken_ReturnsSuccess() throws URISyntaxException {
        when(requestHandler.deletePermissions(startsWith(EGA_ACCOUNT_ID_PREFIX), anyString()))
                .thenReturn(ok().build());
        final String accessToken = obtainAccessToken();

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("value", "dummy-value");

        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, getAccessTokenHeader(accessToken));

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), DELETE, httpEntity, Void.class), OK);
    }

    @Test
    public void deletePermissions_WhenPassEGAAccountIdWithoutAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), DELETE, null, Void.class), UNAUTHORIZED);
    }

    @Test
    public void deletePermissions_WhenPassEGAAccountIdWithInvalidAccessToken_FailsWithUnauthorized() throws URISyntaxException {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getAccessTokenHeader(getInvalidAccessToken()));

        assertStatus(restTemplate.exchange(getPermissionsURI(EGA_ACCOUNT_ID), DELETE, httpEntity, Void.class), UNAUTHORIZED);
    }
    
    private void commonMock() {
        doNothing()
                .when(requestHandler)
                .verifyAccountId(startsWith(EGA_ACCOUNT_ID_PREFIX));
        when(permissionsService.getVisas(startsWith(EGA_ACCOUNT_ID_PREFIX)))
                .thenReturn(singletonList(mock(Visa.class)));
        when(jwtService.createJWT(any(Visa.class)))
                .thenReturn(mock(SignedJWT.class));
    }

    private <T> void assertStatus(final ResponseEntity<T> response, final HttpStatus httpStatus) {
        assertThat(response)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(httpStatus);
    }

    private String obtainAccessToken() throws URISyntaxException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", CLIENT_CREDENTIALS.getValue());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", scope);

        final HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        tokenHeaders.set(AUTHORIZATION, httpBasicAuth(clientId, clientSecret));

        final URI requestURI = UriComponentsBuilder
                .fromUri(new URI(applicationTestURL))
                .path("/oauth2/token")
                .build()
                .toUri();

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, tokenHeaders);
        final ResponseEntity<String> response = restTemplate
                .postForEntity(requestURI, request, String.class);

        final JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(response.getBody()).get("access_token").toString();
    }

    private HttpHeaders getAccessTokenHeader(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, "Bearer " + accessToken);
        return headers;
    }

    private String httpBasicAuth(final String clientId, final String clientSecret) {
        final byte[] toEncode = (clientId + ":" + clientSecret).getBytes(UTF_8);
        return "Basic " + new String(getEncoder().encode(toEncode));
    }

    private URI getPermissionsURI(String accountId) throws URISyntaxException {
        return UriComponentsBuilder
                .fromUri(new URI(applicationTestURL))
                .path("/jwt/{accountId}/permissions")
                .build(accountId);
    }

    private String getInvalidAccessToken() {
        return "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhc2h1dG9zaEBlYmkuYWMudWsiLCJhenAiOiJmMjB" +
                "jZDJkMy02ODJhLTQ1NjgtYTUzZS00MjYyZWY1NGM4ZjQiLCJpc3MiOiJodHRwczpcL1wvZWdhLmViaS5hYy51azo4" +
                "NDQzXC9lZ2Etb3BlbmlkLWNvbm5lY3Qtc2VydmVyXC8iLCJleHAiOjE2MDA4NjM0ODgsImlhdCI6MTYwMDg1OTg4O" +
                "CwianRpIjoiZmY0NzYwZGMtNzFkYy00ZTFkLThjNDgtNGZiYmE2NDg0YmE0In0.Q9Sj478TgrdY0B3laiQqejrSao" +
                "W1rYJyXwk4Koka-izdp4HGmzfKaNFGgI32gk811Faro5S4QToE_ll3zvHN7JPBQ1V6AKou5ilZGSTAELDiUZjmfSA" +
                "y3EyW9Z5wxljp-8tgCvQbQeAFCDrVN3asVRYgJvA4exiqd5IXDm1b6X0";
    }
}
