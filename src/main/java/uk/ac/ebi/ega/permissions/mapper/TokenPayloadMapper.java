/*
 * Copyright 2020-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.PassportClaim;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TokenPayloadMapper {

    @Mapping(source = "accountId", target = "passportClaimId.accountId")
    PassportClaim mapPassportVisaObjectToPassportClaim(String accountId, PassportVisaObject passportVisaObject);

    default List<PassportClaim> mapPassportVisaObjectsToPassportClaims(String accountId, List<PassportVisaObject> passportVisaObjects) {
        return passportVisaObjects.stream().map(e -> this.mapPassportVisaObjectToPassportClaim(accountId, e)).collect(Collectors.toList());
    }

    PassportVisaObject mapPassportClaimToPassportVisaObject(PassportClaim passportClaim);

    List<PassportVisaObject> mapPassportClaimsToPassportVisaObjects(List<PassportClaim> passportClaims);

    default Visa mapJWTClaimSetToVisa(JWTClaimsSet jwtClaimsSet) {
        ObjectMapper mapper = new ObjectMapper();
        Visa visa = null;
        try {
            visa = mapper.readValue(jwtClaimsSet.toString(), Visa.class);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonProcessingException.printStackTrace();
        }
        return visa;
    }

    AccountAccess mapPassportClaimToAccountAccess(PassportClaim passportClaim);

    List<AccountAccess> mapPassportClaimsToAccountAccesses(List<PassportClaim> passportClaims);
}
