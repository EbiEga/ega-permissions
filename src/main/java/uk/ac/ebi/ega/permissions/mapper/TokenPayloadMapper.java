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
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.Authority;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.PassportClaimId;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.VisaType;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.PassportClaim;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TokenPayloadMapper {

	default PassportClaim mapPassportVisaObjectToPassportClaim(String accountId,
			PassportVisaObject passportVisaObject) {
		final PassportClaim pc = new PassportClaim();
		pc.setPassportClaimId(new PassportClaimId(accountId, passportVisaObject.getValue()));
		pc.setAsserted(passportVisaObject.getAsserted());
		pc.setBy(Authority.valueOf(passportVisaObject.getBy()));
		pc.setSource(passportVisaObject.getSource());
		pc.setType(VisaType.valueOf(passportVisaObject.getType()));
		return pc;
	};

	default List<PassportClaim> mapPassportVisaObjectsToPassportClaims(String accountId,
			List<PassportVisaObject> passportVisaObjects) {
		return passportVisaObjects.stream().map(e -> this.mapPassportVisaObjectToPassportClaim(accountId, e))
				.collect(Collectors.toList());
	}

	default PassportVisaObject mapPassportClaimToPassportVisaObject(PassportClaim passportClaim) {
		final PassportVisaObject pvo = new PassportVisaObject();
		pvo.setAsserted(passportClaim.getAsserted());
		pvo.setBy(passportClaim.getBy().name());
		pvo.setSource(passportClaim.getSource());
		pvo.setType(passportClaim.getType().name());
		pvo.setValue(passportClaim.getPassportClaimId().getValue());
		return pvo;
	};

	default List<PassportVisaObject> mapPassportClaimsToPassportVisaObjects(List<PassportClaim> passportClaims) {
		return passportClaims.stream().map(pc -> mapPassportClaimToPassportVisaObject(pc)).collect(Collectors.toList());
	};

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

	default AccountAccess mapPassportClaimToAccountAccess(PassportClaim passportClaim) {
		final AccountAccess ac = new AccountAccess();
		ac.setAccountId(passportClaim.getPassportClaimId().getAccountId());
		ac.setAsserted(passportClaim.getAsserted());
		return ac;
	};

	default List<AccountAccess> mapPassportClaimsToAccountAccesses(List<PassportClaim> passportClaims) {
		return passportClaims.stream().map(pc -> mapPassportClaimToAccountAccess(pc)).collect(Collectors.toList());
	};
}
