package uk.ac.ebi.ega.permissions.controller;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ega.permissions.model.JWTTokenDTO;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PermissionsController {

    public JWTService jwtService;
    public PermissionsService permissionsService;

    public PermissionsController(PermissionsService permissionsService, JWTService jwtService) {
        this.permissionsService = permissionsService;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/user/{userId}/permissions/ga4gh", produces = MediaType.APPLICATION_JSON_VALUE)
    public JWTTokenDTO getUserInfoAsGA4GH(@PathVariable("userId") String userId) {

        List<Visa> visas = this.permissionsService.getVisas(userId);
        //Create JWT for each dataset
        final List<String> ga4ghClaims = visas
                .stream()
                .map(this::createSignedJWT)
                .map(JWSObject::serialize)
                .collect(Collectors.toList());

        return new JWTTokenDTO(ga4ghClaims);
    }

    private SignedJWT createSignedJWT(final Visa visa) {
        //Create JWT token
        final SignedJWT ga4ghSignedJWT = jwtService.createJWT(visa);

        //Sign JWT token by signer
        jwtService.signJWT(ga4ghSignedJWT);
        return ga4ghSignedJWT;
    }
}
