package uk.ac.ebi.ega.permissions.controller.delegate;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.AccountIdApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.*;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.util.List;
import java.util.stream.Collectors;

public class AccountIdApiDelegateImpl implements AccountIdApiDelegate {

    private final PermissionsService permissionsService;
    private final JWTService jwtService;
    private final RequestHandler requestHandler;

    public AccountIdApiDelegateImpl(PermissionsService permissionsService, JWTService jwtService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.jwtService = jwtService;
        this.requestHandler = requestHandler;
    }

    public ResponseEntity<Visas> readPermissions(String accountId, Format format) {
        Visas response = new Visas();

        if (format == null) {
            format = Format.JWT;
        }

        accountId = requestHandler.getAccountIdForElixirId(accountId);
        requestHandler.verifyAccountId(accountId);
        List<Visa> visas = this.permissionsService.getVisas(accountId);

        List<OneOfVisasItems> jwtItems;

        switch (format) {
            case JWT:
                response.addAll(
                        visas.stream()
                                .map(this::createSignedJWT)
                                .map(JWSObject::serialize)
                                .map(e -> {
                                    JWTVisa jwtVisa = new JWTVisa();
                                    jwtVisa.setJwt(e);
                                    return jwtVisa;
                                })
                                .collect(Collectors.toList())
                );
                break;
            case PLAIN:
                response.addAll(visas);
                break;
        }

        return ResponseEntity.ok(response);
    }

    private SignedJWT createSignedJWT(final Visa visa) {
        //Create JWT token
        final SignedJWT ga4ghSignedJWT = jwtService.createJWT(visa);

        //Sign JWT token by signer
        jwtService.signJWT(ga4ghSignedJWT);
        return ga4ghSignedJWT;
    }
}
