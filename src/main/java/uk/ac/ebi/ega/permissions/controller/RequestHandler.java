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
package uk.ac.ebi.ega.permissions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.ServiceException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.SystemException;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.Account;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.service.AccessGroupDataService;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.HasReadOrWritePermissions;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.HasWritePermissions;
import uk.ac.ebi.ega.permissions.mapper.AccessGroupMapper;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.AccessGroup;
import uk.ac.ebi.ega.permissions.model.Format;
import uk.ac.ebi.ega.permissions.model.JWTPassportVisaObject;
import uk.ac.ebi.ega.permissions.model.JWTPermissionsResponse;
import uk.ac.ebi.ega.permissions.model.JWTVisa;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.PermissionsResponses;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.model.Visas;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.SecurityService;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class RequestHandler {

    public static final String EGA_ACCOUNT_ID_PREFIX = "EGAW";
    public static final String ELIXIR_ACCOUNT_SUFFIX = "@elixir-europe.org";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private final PermissionsService permissionsService;
    private final TokenPayloadMapper tokenPayloadMapper;
    private final AccessGroupDataService userGroupDataService;
    private final JWTService jwtService;
    private final SecurityService securityService;
    private final AccessGroupMapper accessGroupMapper;

    public RequestHandler(final PermissionsService permissionsService,
                          final TokenPayloadMapper tokenPayloadMapper,
                          final AccessGroupMapper accessGroupMapper,
                          final AccessGroupDataService userGroupDataService,
                          final JWTService jwtService,
                          final SecurityService securityService) {
        this.permissionsService = permissionsService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.userGroupDataService = userGroupDataService;
        this.jwtService = jwtService;
        this.securityService = securityService;
        this.accessGroupMapper = accessGroupMapper;
    }

    public ResponseEntity<Visas> getPermissionForCurrentUser(Format format) {
        String currentUser = securityService.getCurrentUser().orElseThrow(() -> new ServiceException("Operation not allowed for Anonymous users"));
        String userAccountId = permissionsService.getAccountByEmail(currentUser).orElseThrow(() -> new ServiceException("Current user is not allowed to access this resource")).getAccountId();
        List<Visa> visas = this.permissionsService.getVisas(userAccountId);
        return this.getPermissions(visas, format);
    }

    public ResponseEntity<List<AccessGroup>> getGroupsForCurrentUser() {
        String currentUser = securityService.getCurrentUser().orElseThrow(() -> new ServiceException("Operation not allowed for Anonymous users"));
        String userAccountId = permissionsService.getAccountByEmail(currentUser).orElseThrow(() -> new ServiceException("Current user is not allowed to access this resource")).getAccountId();
        return ResponseEntity.ok(this.accessGroupMapper.accessGroupsFromAccessGroupEntities(this.userGroupDataService.getPermissionGroups(userAccountId)));
    }

    @HasReadOrWritePermissions
    public ResponseEntity<Visas> getPermissionsForUser(String userId, Format format) {
        String userAccountId = getAccountIdForElixirId(userId);
        verifyAccountId(userAccountId);
        List<Visa> visas = this.permissionsService.getControlledVisas(userAccountId, getControllerAccountId());
        return this.getPermissions(visas, format);
    }

    public ResponseEntity<Visas> getPermissions(List<Visa> visas, Format format) {
        Visas response = new Visas();

        if (format == null || format == Format.JWT) {
            response.addAll(
                    visas.stream()
                            .map(this::createSignedJWT)
                            .map(JWSObject::serialize)
                            .map(e -> {
                                JWTVisa jwtVisa = new JWTVisa();
                                jwtVisa.setJwt(e);
                                jwtVisa.setFormat(format);
                                return jwtVisa;
                            })
                            .collect(Collectors.toList())
            );
        } else {
            response.addAll(visas);
        }

        return ResponseEntity.ok(response);
    }

    @HasWritePermissions
    public ResponseEntity<PermissionsResponses> createPermissions(String userId, List<Object> body, Format format) {

        PermissionsResponses responses = new PermissionsResponses();
        List<PassportVisaObject> passportVisaObjects;
        List<String> passportVisaStrings;

        ObjectMapper mapper = new ObjectMapper();

        //TODO: Handle mapper exception and improve conversion
        if (format == null || format == Format.PLAIN) {
            passportVisaObjects = body.parallelStream().map(v -> mapper.convertValue(v, PassportVisaObject.class)).collect(Collectors.toList());
            List<PermissionsResponse> permissionResponses = createPlainPermissions(getAccountIdForElixirId(userId), passportVisaObjects);
            responses.addAll(permissionResponses);

        } else if (format == Format.JWT) {
            passportVisaStrings = body.parallelStream().map(v -> mapper.convertValue(v, JWTPassportVisaObject.class).getJwt()).collect(Collectors.toList());
            List<JWTPermissionsResponse> jwtResponses = createJWTPermissions(getAccountIdForElixirId(userId), passportVisaStrings);
            responses.addAll(jwtResponses);
        }

        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responses);
    }

    public List<JWTPermissionsResponse> createJWTPermissions(String accountId, List<String> ga4ghVisaV1List) {
        return ga4ghVisaV1List
                .stream()
                .map(strVisa -> {
                    try {
                        Visa visa = tokenPayloadMapper.mapJWTClaimSetToVisa(SignedJWT.parse(strVisa).getJWTClaimsSet());
                        PermissionsResponse preResponse = handlePassportVisaObjectProcessing(accountId, visa.getGa4ghVisaV1());
                        JWTPermissionsResponse response = new JWTPermissionsResponse();
                        response.setGa4ghVisaV1(strVisa);
                        response.setStatus(preResponse.getStatus());
                        response.setMessage(preResponse.getMessage());
                        response.setFormat(Format.JWT);
                        return response;
                    } catch (ParseException ex) {
                        JWTPermissionsResponse response = new JWTPermissionsResponse();
                        response.setGa4ghVisaV1(strVisa);
                        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                        response.setMessage("Error decoding the JWT Token");
                        return response;
                    }
                }).collect(Collectors.toList());
    }

    public List<PermissionsResponse> createPlainPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        return passportVisaObjects
                .stream()
                .map(passportVisaObject -> handlePassportVisaObjectProcessing(accountId, passportVisaObject))
                .collect(Collectors.toList());
    }

    @HasWritePermissions
    public ResponseEntity<Void> deletePermissions(String userId, List<String> values) {
        verifyAccountId(userId);
        if (values.contains("all")) { //ignore all other values and remove all permissions
            values = this.getAllPermissionsForUser(userId);
        } else {
            values.forEach(this::validateDatasetBelongsToDAC);
        }
        this.permissionsService.deletePassportVisaObject(userId, values);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private PermissionsResponse handlePassportVisaObjectProcessing(String userAccountId, PassportVisaObject passportVisaObject) {
        try {
            this.permissionsService.savePassportVisaObject(getControllerAccountId(), userAccountId, passportVisaObject);
            final PermissionsResponse permissionsResponse = getPermissionsResponse(HttpStatus.CREATED, "Created");
            permissionsResponse.setGa4ghVisaV1(passportVisaObject);
            return permissionsResponse;
        } catch (AccessDeniedException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return getPermissionsResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return getPermissionsResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (SystemException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return getPermissionsResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private PermissionsResponse getPermissionsResponse(final HttpStatus httpStatus, final String message) {
        final PermissionsResponse permissionsResponse = new PermissionsResponse();
        permissionsResponse.setStatus(httpStatus.value());
        permissionsResponse.setMessage(message);
        permissionsResponse.setFormat(Format.PLAIN);
        return permissionsResponse;
    }

    public void verifyAccountId(String userAccountId) {
        if (!this.permissionsService.accountExist(userAccountId)) {
            throw new ValidationException("User account invalid or not found");
        }
    }

    public String getAccountIdForElixirId(String accountId) {
        if (!accountId.startsWith(EGA_ACCOUNT_ID_PREFIX)) {
            AccountElixirId accountIdForElixirId =
                    permissionsService.getAccountIdForElixirId(accountId)
                            .orElseThrow(() -> new ValidationException("No linked EGA account for accountId ".concat(accountId)));
            return accountIdForElixirId.getAccountId();
        }
        return accountId;
    }

    public void validateDatasetBelongsToDAC(String datasetId) {
        String bearerAccountId = getBearerAccountId();
        if (!userGroupDataService.isEGAAdmin(bearerAccountId) && !userGroupDataService.datasetBelongsToDAC(bearerAccountId, datasetId)) {
            throw new ValidationException("User doesn't own dataset.");
        }
    }

    private List<String> getAllPermissionsForUser(String accountId) {
        return permissionsService.getPermissionByAccountIdAndController(accountId, getBearerAccountId());
    }

    private String getBearerAccountId() {
        String email = securityService.getCurrentUser().orElseThrow(() -> new ValidationException("Anonymous user not allowed."));
        if (email.toLowerCase().endsWith(ELIXIR_ACCOUNT_SUFFIX)) {
            AccountElixirId accountElixirId = permissionsService.getAccountIdForElixirId(email).orElseThrow(() -> new ValidationException("Account not found."));
            return accountElixirId.getAccountId();
        } else {
            Account account = permissionsService.getAccountByEmail(email).orElseThrow(() -> new ValidationException("Account not found."));
            return account.getAccountId();
        }
    }

    private SignedJWT createSignedJWT(final Visa visa) {
        //Create JWT token
        final SignedJWT ga4ghSignedJWT = this.jwtService.createJWT(visa);

        //Sign JWT token by signer
        this.jwtService.signJWT(ga4ghSignedJWT);
        return ga4ghSignedJWT;
    }

    private String getControllerAccountId() {
        String controllerEmail = securityService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Invalid controller"));
        Account controllerAccount = permissionsService.getAccountByEmail(controllerEmail).orElseThrow(() ->
                new AccessDeniedException(("No linked EGA account for email ").concat(controllerEmail)));
        return controllerAccount.getAccountId();
    }
}
