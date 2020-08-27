package uk.ac.ebi.ega.permissions.service;

import com.nimbusds.jwt.SignedJWT;
import uk.ac.ebi.ega.permissions.exception.JWTException;
import uk.ac.ebi.ega.permissions.model.Visa;

public interface JWTService {

    SignedJWT createJWT(Visa jwtData);

    void signJWT(SignedJWT signedJWT) throws JWTException;

    boolean isValidSignature(SignedJWT signedJWT);

}
