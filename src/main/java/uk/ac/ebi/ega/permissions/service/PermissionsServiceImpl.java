/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.ServiceException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.SystemException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.Account;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.Event;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.service.EventDataService;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.cache.aop.annotation.UpdateCacheAfterCreatePermission;
import uk.ac.ebi.ega.permissions.cache.aop.annotation.UpdateCacheAfterDeletePermission;
import uk.ac.ebi.ega.permissions.configuration.VisaInfoProperties;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.Format;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PermissionsServiceImpl implements PermissionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsServiceImpl.class);

    private static final String ELIXIR_ACCOUNT_SUFFIX = "@elixir-europe.org";
    private static final String EVENT_SAVED = "SAVED";
    private static final String EVENT_DELETED = "DELETED";

    private final PermissionsDataService permissionsDataService;
    private final EventDataService eventDataService;
    private final TokenPayloadMapper tokenPayloadMapper;
    private final VisaInfoProperties visaInfoProperties;
    private final SecurityService securityService;

    public PermissionsServiceImpl(final PermissionsDataService permissionsDataService,
                                  final EventDataService eventDataService,
                                  final TokenPayloadMapper tokenPayloadMapper,
                                  final VisaInfoProperties visaInfoProperties,
                                  final SecurityService securityService) {
        this.permissionsDataService = permissionsDataService;
        this.eventDataService = eventDataService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.visaInfoProperties = visaInfoProperties;
        this.securityService = securityService;
    }

    @Override
    public boolean accountExist(String accountId) {
        return this.permissionsDataService.accountExists(accountId);
    }

    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return permissionsDataService.getAccountByEmail(email);
    }

    @Override
    public Optional<AccountElixirId> getAccountIdForElixirId(String elixirId) {
        return permissionsDataService.getAccountIdForElixirId(elixirId);
    }

    @Override
    public List<Visa> getVisas(String userAccountId) {
        List<PassportVisaObject> passportVisaObjects = this.tokenPayloadMapper
                .mapPassportClaimsToPassportVisaObjects(this.permissionsDataService.getPassportClaimsForAccount(userAccountId));
        return formatVisas(userAccountId, passportVisaObjects);
    }

    @Override
    public List<Visa> getControlledVisas(String userAccountId, String controllerAccountId) {
        List<PassportVisaObject> passportVisaObjects = this.tokenPayloadMapper
                .mapPassportClaimsToPassportVisaObjects(this.permissionsDataService.getPassportClaimsForAccountAndController(userAccountId, controllerAccountId));
        return formatVisas(userAccountId, passportVisaObjects);
    }

    private List<Visa> formatVisas(String userAccountId, List<PassportVisaObject> passportVisaObjects) {
        if (CollectionUtils.isEmpty(passportVisaObjects)) {
            return Collections.emptyList();
        }

        Visa visa = generatedVisaInfo(userAccountId);

        return passportVisaObjects.stream().map(e -> {
            Visa innerVisa = new Visa();
            innerVisa.setJti(visa.getJti());
            innerVisa.setIss(visa.getIss());
            innerVisa.setExp(visa.getExp());
            innerVisa.setSub(visa.getSub());
            innerVisa.setIat(visa.getIat());
            innerVisa.setFormat(Format.PLAIN);
            e.setFormat(Format.PLAIN);
            innerVisa.setGa4ghVisaV1(e);
            return innerVisa;
        }).collect(Collectors.toList());

    }

    @UpdateCacheAfterCreatePermission
    @Override
    @Transactional
    public PassportVisaObject savePassportVisaObject(String controllerAccountId, String userAccountId, PassportVisaObject passportVisaObject) throws ServiceException, SystemException {
        try {
            //TODO: This will be improved later with other validations such as valid accountIds and Datasets
            if (passportVisaObject.getValue().isEmpty() || userAccountId.isEmpty()) {
                throw new ValidationException("Values for accountId and value are incorrect or not valid");
            }
            if (!this.permissionsDataService.userCanControlDataset(controllerAccountId, passportVisaObject.getValue())) {
                throw new AccessDeniedException("User cannot control access to dataset");
            }
            PassportClaim savedClaim = this.permissionsDataService.savePassportClaim(tokenPayloadMapper.mapPassportVisaObjectToPassportClaim(userAccountId, passportVisaObject));
            passportVisaObject = this.tokenPayloadMapper.mapPassportClaimToPassportVisaObject(savedClaim);
            eventDataService.saveEvent(getEvent(userAccountId, new ObjectMapper().writeValueAsString(passportVisaObject), EVENT_SAVED));
        } catch (PersistenceException | CannotCreateTransactionException | IllegalArgumentException ex) { //These are spring-data possible errors
            throw new ServiceException(String.format("Error saving permissions to the DB for [account:%s, object:%s]", userAccountId, passportVisaObject.getValue()), ex);
        } catch (ValidationException | AccessDeniedException exception) {
            throw exception;
        } catch (Exception ex) { //Generic errors are wrapped with a default message
            throw new SystemException(String.format("Error processing permissions for [account:%s, object:%s]", userAccountId, passportVisaObject.getValue()), ex);
        }
        return passportVisaObject;
    }

    @UpdateCacheAfterDeletePermission
    @Override
    @Transactional
    public void deletePassportVisaObject(String accountId, List<String> toDeleteValues) {
        try {
            List<PassportClaim> deletedClaims = toDeleteValues
                    .stream()
                    .map(val -> permissionsDataService.deletePassportClaim(accountId, val))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if (!toDeleteValues.isEmpty() && deletedClaims.isEmpty()) {
                throw new ValidationException("Values for accountId and value are incorrect or not valid");
            } else if (toDeleteValues.size() != deletedClaims.size()) {
                LOGGER.warn("Some values provided trying to delete permissions might be invalid: {}", String.join(",", toDeleteValues));
            }
            eventDataService.saveEvent(getEvent(accountId, new ObjectMapper().writeValueAsString(deletedClaims), EVENT_DELETED));
        } catch (Exception ex) {
            throw new SystemException(String.format("Error processing the request for [accountId:%s, values:%s]", accountId, String.join(",", toDeleteValues)), ex);
        }
    }

    @Override
    public List<AccountAccess> getGrantedAccountsForDataset(String datasetId) {
        return this.tokenPayloadMapper.mapPassportClaimsToAccountAccesses(this.permissionsDataService.getPassportClaimsForDataset(datasetId));
    }

    @Override
    public List<String> getPermissionByAccountIdAndController(String accountId, String egaAccountStableId) {
        return this.permissionsDataService.getPassportClaimsByUserAndController(accountId, egaAccountStableId).stream().map(pc -> pc.getPassportClaimId().getValue()).collect(Collectors.toList());
    }

    //TODO: Verify/improve this logic to populate visa attributes
    // this can be generated but for now I'm using values from properties
    private Visa generatedVisaInfo(String accountId) {
        Visa visa = new Visa();
        visa.setSub(accountId);
        visa.setIss(this.visaInfoProperties.getIssuer());
        visa.setExp(Calendar.getInstance().getTimeInMillis() / 1000L + this.visaInfoProperties.getExpireAfter());
        visa.setIat(this.visaInfoProperties.getIat());
        visa.setJti(UUID.randomUUID().toString());
        return visa;
    }

    private Event getEvent(String userId, String data, String method) {
        Event events = new Event();
        events.setBearerId(getBearerAccountId());
        events.setData(data == null ? null : data.replace("\n", ""));
        events.setMethod(method);
        events.setUserId(userId);
        return events;
    }

    private String getBearerAccountId() {
        String email = securityService.getCurrentUser().orElseThrow(() -> new ValidationException("Invalid user"));

        if (email.toLowerCase().endsWith(ELIXIR_ACCOUNT_SUFFIX)) {
            AccountElixirId accountElixirId = getAccountIdForElixirId(email).orElseThrow(() -> new ValidationException("Account not found."));
            return accountElixirId.getAccountId();
        } else {
            Account account = getAccountByEmail(email).orElseThrow(() -> new ValidationException("Account not found."));
            return account.getAccountId();
        }
    }
}
