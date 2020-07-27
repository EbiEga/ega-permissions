package uk.ac.ebi.ega.permissions.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.TokenPayload;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TokenPayloadMapperTest {

    @Test
    void mapVisaToTokenPayload() {
        PassportVisaObject passportVisaObject1 = new PassportVisaObject();
        passportVisaObject1.setSource("https://ega-archive.org/dacs/EGAC00001111111");
        passportVisaObject1.setType("ControlledAccessGrants");
        passportVisaObject1.setValue("https://ega-archive.org/datasets/EGAD00002222222");
        passportVisaObject1.setAsserted(1568814383);
        passportVisaObject1.setBy("dac");

        Visa visa = new Visa();
        visa.setSub("EGAW00000015388");
        visa.setIss("https://ega.ebi.ac.uk:8053/ega-openid-connect-server/");
        visa.setExp(1592824514);
        visa.setIat(1592820914);
        visa.setJti("f030c620-993b-49af-a830-4b9af4f379f8");
        visa.setGa4ghVisaV1(passportVisaObject1);

        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        TokenPayload tokenPayload = mapper.mapVisa(visa);
        tokenPayload.setClaims(mapper.mapPassportVisaObjectToList(visa.getGa4ghVisaV1()));

        assertThat(tokenPayload.getSub()).isEqualTo("EGAW00000015388");
    }

    @Test
    void mapPassportClaims(){
        PassportClaim claim1 = new PassportClaim("ControlledAccessGrants", 1568814383, "https://ega-archive.org/datasets/EGAD00002222222", "https://ega-archive.org/dacs/EGAC00001111111", "dac");
        PassportClaim claim2 = new PassportClaim("ControlledAccessGrants", 1768814383, "https://ega-archive.org/datasets/EGAD00003333333", "https://ega-archive.org/dacs/EGAC00001111111", "dac");

        TokenPayloadMapper mapper = Mappers.getMapper(TokenPayloadMapper.class);
        List<PassportVisaObject> passportVisaObjects = mapper.mapPassportClaims(Arrays.asList(claim1, claim2));

        assertThat(passportVisaObjects).hasSize(2);
        assertThat(passportVisaObjects).filteredOn(e->e.getValue().equals("https://ega-archive.org/datasets/EGAD00002222222")).hasSize(1);
        assertThat(passportVisaObjects).filteredOn(e->e.getValue().equals("https://ega-archive.org/datasets/EGAD00003333333")).hasSize(1);
    }
}