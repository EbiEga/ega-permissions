package uk.ac.ebi.ega.permissions.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.util.StringUtils;
import uk.ac.ebi.ega.permissions.exception.JWTException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.JWTAlgorithm;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JWTServiceImpl implements JWTService {

    private String defaultSignerKeyId;
    private final JWTAlgorithm defaultJWTAlgorithm;
    private final URI jkuURI;

    // map of identifier to signer
    private final Map<String, JWSSigner> signers = new HashMap<>();

    // map of identifier to verifier
    private final Map<String, JWSVerifier> verifiers = new HashMap<>();

    public JWTServiceImpl(final String jwks,
                          final String defaultSignerKeyId,
                          final JWTAlgorithm defaultJWTAlgorithm,
                          final URL jkuURL) throws ParseException, URISyntaxException {
        this.defaultSignerKeyId = defaultSignerKeyId;
        this.defaultJWTAlgorithm = defaultJWTAlgorithm;
        this.jkuURI = jkuURL.toURI();
        buildSignersAndVerifiers(buildJWKKeyMap(jwks));
    }

    @Override
    public SignedJWT createJWT(final Visa visa) {
        return doCreateJWT(visa);
    }

    private SignedJWT doCreateJWT(final Visa visa) throws SystemException {
        final JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(defaultJWTAlgorithm.name()))
                .keyID(defaultSignerKeyId)
                .type(JOSEObjectType.JWT)
                .jwkURL(jkuURI)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(visa);

        final JWTClaimsSet jwtClaimsSet;

        try {
            jwtClaimsSet = JWTClaimsSet.parse(node.toString());
        } catch (ParseException e) {
            throw new SystemException(e.getMessage(), e);
        }
        return new SignedJWT(jwsHeader, jwtClaimsSet);
    }

    @Override
    public void signJWT(final SignedJWT signedJWT) throws JWTException {
        if (defaultSignerKeyId == null) {
            throw new IllegalStateException("Tried to call default signing with no default signer ID set");
        }

        final JWSSigner signer = signers.get(defaultSignerKeyId);
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new JWTException("Failed to sign JWT, error was: ".concat(e.getMessage()), e);
        }
    }

    @Override
    public boolean isValidSignature(final SignedJWT signedJWT) {
        throw new UnsupportedOperationException("This operation is not supported at the moment");
    }

    private Map<String, JWK> buildJWKKeyMap(final String jwks) throws ParseException {
        final JWKSet jwkSet = JWKSet.parse(jwks);

        // Map of identifier to key
        final Map<String, JWK> keys = new HashMap<>();

        // Convert all keys in the keystore to a map based on key id
        for (final JWK key : jwkSet.getKeys()) {
            if (!StringUtils.isEmpty(key.getKeyID())) {
                keys.put(key.getKeyID(), key);
            } else {
                throw new IllegalArgumentException("Tried to load a key from a keystore without a 'kid' field: " + key);
            }
        }
        return Collections.unmodifiableMap(keys);
    }

    private void buildSignersAndVerifiers(final Map<String, JWK> keysMap) throws JWTException {
        verifyAndAssignDefaultSigner(keysMap);

        for (final Map.Entry<String, JWK> jwkEntry : keysMap.entrySet()) {
            final String keyId = jwkEntry.getKey();
            final JWK jwk = jwkEntry.getValue();

            try {
                if (jwk instanceof RSAKey) {
                    // build RSA signers & verifiers
                    if (jwk.isPrivate()) { // only add the signer if there's a private key
                        RSASSASigner signer = new RSASSASigner((RSAKey) jwk);
                        signers.put(keyId, signer);
                    }
                    final RSASSAVerifier verifier = new RSASSAVerifier((RSAKey) jwk);
                    verifiers.put(keyId, verifier);
                } else {
                    throw new JWTException("Only RSA JWK is supported");
                }
            } catch (Exception e) {
                throw new JWTException("Error while building signers & verifiers. ".concat(e.getMessage()), e);
            }
        }
    }

    private void verifyAndAssignDefaultSigner(final Map<String, JWK> keysMap) throws JWTException {
        if (keysMap.size() == 0) {
            throw new JWTException("At least one Key ID should exist");
        } else if (keysMap.size() == 1) {
            // if there's only one key, it's the default
            defaultSignerKeyId = keysMap.keySet().iterator().next();
        } else if (defaultSignerKeyId == null || keysMap.get(defaultSignerKeyId) == null) {
            // if there are multiple keys, then default singer id should be specified
            throw new JWTException("Multiple keys are found. Specify valid default signer key id");
        }
    }

}
