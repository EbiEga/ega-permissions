package uk.ac.ebi.ega.permissions.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.TokenPayload;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TokenPayloadMapper {

    @Mapping(target = "claims", expression = "java(mapPassportVisaObjectToList(visa.getGa4ghVisaV1()))")
    TokenPayload mapVisa(Visa visa);

    Visa mapTokenPayload(TokenPayload tokenPayload);

    PassportClaim mapPassportVisaObject(PassportVisaObject passportVisaObject);

    default List<PassportClaim> mapPassportVisaObjectToList(PassportVisaObject passportVisaObject) {
        return Arrays.asList(mapPassportVisaObject(passportVisaObject));
    }

    PassportVisaObject mapPassportClaim(PassportClaim passportClaim);

    List<PassportVisaObject> mapPassportClaims(List<PassportClaim> passportClaims);
}
