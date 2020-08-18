package uk.ac.ebi.ega.permissions.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

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
        PassportVisaObject passportVisaObject1 = new PassportVisaObject();
        passportVisaObject1.setBy("dac");
        passportVisaObject1.setAsserted(1568814383L);
        passportVisaObject1.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject1.setType("ControlledAccessGrants");
        passportVisaObject1.setSource("https://ega-archive.org/dacs/EGAC00001111111");

        PassportVisaObject passportVisaObject2 = new PassportVisaObject();
        passportVisaObject2.setBy("dac");
        passportVisaObject2.setAsserted(1568814383L);
        passportVisaObject2.setValue("https://ega-archive.org/datasets/EGAD00003333333");
        passportVisaObject2.setType("ControlledAccessGrants");
        passportVisaObject2.setSource("https://ega-archive.org/dacs/EGAC00001111111");


        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        List<PassportClaim> claims = mapper.mapPassportVisaObjectsToPassportClaims("testAccountId", Arrays.asList(passportVisaObject1, passportVisaObject2));

        assertThat(claims).hasSize(2);
        assertThat(claims.get(0).getAccountId()).isEqualTo("testAccountId");
    }
}