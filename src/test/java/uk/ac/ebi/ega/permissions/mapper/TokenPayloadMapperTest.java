package uk.ac.ebi.ega.permissions.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenPayloadMapperTest {

    @Test
    void mapPassportClaimsToPassportVisaObjects() {
        PassportClaim claim1 = new PassportClaim("testAccountId", PassportClaim.VisaType.ControlledAccessGrants, 1568814383L, "https://ega-archive.org/datasets/EGAD00002222222", "https://ega-archive.org/dacs/EGAC00001111111", PassportClaim.Authority.dac);
        PassportClaim claim2 = new PassportClaim("testAccountId", PassportClaim.VisaType.ControlledAccessGrants, 1768814383L, "https://ega-archive.org/datasets/EGAD00003333333", "https://ega-archive.org/dacs/EGAC00001111111", PassportClaim.Authority.dac);

        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        List<PassportVisaObject> passportVisaObjects = mapper.mapPassportClaimsToPassportVisaObjects(Arrays.asList(claim1, claim2));

        assertThat(passportVisaObjects).hasSize(2);
        assertThat(passportVisaObjects).filteredOn(e -> e.getValue().equals("https://ega-archive.org/datasets/EGAD00002222222")).hasSize(1);
        assertThat(passportVisaObjects).filteredOn(e -> e.getValue().equals("https://ega-archive.org/datasets/EGAD00003333333")).hasSize(1);
    }

    @Test
    void mapPassportVisaObjectsToPassportClaims() {
        PassportVisaObject passportVisaObject1 = createPassportVisaObject("https://ega-archive.org/datasets/EGAD00002222222");
        PassportVisaObject passportVisaObject2 = createPassportVisaObject("https://ega-archive.org/datasets/EGAD00003333333");

        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        List<PassportClaim> claims = mapper.mapPassportVisaObjectsToPassportClaims("testAccountId", Arrays.asList(passportVisaObject1, passportVisaObject2));

        assertThat(claims).hasSize(2);
        assertThat(claims.get(0).getAccountId()).isEqualTo("testAccountId");
    }

    @Test
    void mapJWTClaimSetToVisa() throws URISyntaxException, ParseException {
        SignedJWT signedJWT = createSignedJWT();
        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        Visa visa = mapper.mapJWTClaimSetToVisa(signedJWT.getJWTClaimsSet());
        assertThat(visa.getGa4ghVisaV1().getValue()).isEqualTo("https://ega-archive.org/datasets/EGAD00002222222");
        assertThat(visa.getGa4ghVisaV1().getType()).isEqualTo("ControlledAccessGrants");
        assertThat(visa.getIss()).isEqualTo("iss-test");
    }

    @Test
    void mapPassportClaimsToAccountAccesses(){
        PassportClaim claim1 = new PassportClaim("testAccountId1", PassportClaim.VisaType.ControlledAccessGrants, 1568814383L, "https://ega-archive.org/datasets/EGAD00002222222", "https://ega-archive.org/dacs/EGAC00001111111", PassportClaim.Authority.dac);
        PassportClaim claim2 = new PassportClaim("testAccountId2", PassportClaim.VisaType.ControlledAccessGrants, 1768814383L, "https://ega-archive.org/datasets/EGAD00003333333", "https://ega-archive.org/dacs/EGAC00001111111", PassportClaim.Authority.dac);

        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);

        List<AccountAccess> accountAccesses = mapper.mapPassportClaimsToAccountAccesses(Arrays.asList(claim1, claim2));

        assertThat(accountAccesses).hasSize(2);
        assertThat(accountAccesses).filteredOn(e -> e.getAccountId().equals("testAccountId1")).hasSize(1);
        assertThat(accountAccesses).filteredOn(e -> e.getAccountId().equals("testAccountId1")).hasSize(1);
    }

    private PassportVisaObject createPassportVisaObject(String value) {
        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setBy("dac");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setValue(value);
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        return passportVisaObject;
    }

    private SignedJWT createSignedJWT() throws URISyntaxException {
        final JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse("RS256"))
                .keyID("test")
                .type(JOSEObjectType.JWT)
                .jwkURL(new URI("http://example.com/"))
                .build();

        ObjectMapper mapper = new ObjectMapper();

        Visa visa = new Visa();
        visa.setIat(100);
        visa.setSub("sub");
        visa.setIss("iss-test");
        visa.setJti("jti");
        visa.setExp(1000L);
        visa.setGa4ghVisaV1(createPassportVisaObject("https://ega-archive.org/datasets/EGAD00002222222"));

        JsonNode node = mapper.valueToTree(visa);

        final JWTClaimsSet jwtClaimsSet;

        try {
            jwtClaimsSet = JWTClaimsSet.parse(node.toString());
        } catch (ParseException e) {
            throw new SystemException(e.getMessage(), e);
        }
        return new SignedJWT(jwsHeader, jwtClaimsSet);
    }

}