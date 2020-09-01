package uk.ac.ebi.ega.permissions.controller;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ega.permissions.model.JWTTokenResponse;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PermissionsController {

    private JWTService jwtService;
    private PermissionsService permissionsService;
    private RequestHandler requestHandler;

    public PermissionsController(PermissionsService permissionsService, JWTService jwtService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.jwtService = jwtService;
        this.requestHandler = requestHandler;
    }

    @GetMapping(value = "/jwt/{accountId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getUserInfoAsGA4GH(@PathVariable("accountId") String accountId) {
        requestHandler.verifyAccountId(accountId);
        List<Visa> visas = this.permissionsService.getVisas(accountId);
        //Create JWT for each dataset
        final List<String> ga4ghClaims = visas
                .stream()
                .map(this::createSignedJWT)
                .map(JWSObject::serialize)
                .collect(Collectors.toList());

        return ga4ghClaims;
    }

    @PostMapping(value = "/jwt/{accountId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JWTTokenResponse>> createPermissions(@PathVariable("accountId") String accountId,
                                                                    @RequestBody List<String> ga4ghVisaV1List) {
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(requestHandler.createJWTPermissions(accountId, ga4ghVisaV1List));
    }

    @DeleteMapping(value = "/jwt/{accountId}/permissions")
    public ResponseEntity<Void> deletePermissions(@PathVariable("accountId") String accountId,
                                                  @Valid @RequestParam(value = "value") String value) {
        return requestHandler.deletePermissions(accountId, value);
    }

    private SignedJWT createSignedJWT(final Visa visa) {
        //Create JWT token
        final SignedJWT ga4ghSignedJWT = jwtService.createJWT(visa);

        //Sign JWT token by signer
        jwtService.signJWT(ga4ghSignedJWT);
        return ga4ghSignedJWT;
    }
}
