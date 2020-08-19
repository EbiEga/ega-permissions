package uk.ac.ebi.ega.permissions.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TokenPayloadMapper {

    @Mapping(source = "accountId", target = "accountId")
    PassportClaim mapPassportVisaObjectToPassportClaim(String accountId, PassportVisaObject passportVisaObject);

    default List<PassportClaim> mapPassportVisaObjectsToPassportClaims(String accountId, List<PassportVisaObject> passportVisaObjects){
        return passportVisaObjects.stream().map(e -> this.mapPassportVisaObjectToPassportClaim(accountId, e)).collect(Collectors.toList());
    }

    PassportVisaObject mapPassportClaimToPassportVisaObject(PassportClaim passportClaim);

    List<PassportVisaObject> mapPassportClaimsToPassportVisaObjects(List<PassportClaim> passportClaims);
}
