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
import org.springframework.security.access.prepost.PreAuthorize;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.IsAdminReaderOrWriter;
import uk.ac.ebi.ega.permissions.configuration.security.customauthorization.IsAdminWriter;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.*;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
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
    Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private final PermissionsService permissionsService;
    private final TokenPayloadMapper tokenPayloadMapper;
    private final UserGroupDataService userGroupDataService;
    private final JWTService jwtService;
    private final SecurityService securityService;

    public RequestHandler(final PermissionsService permissionsService,
                          final TokenPayloadMapper tokenPayloadMapper,
                          final UserGroupDataService userGroupDataService,
                          final JWTService jwtService,
                          final SecurityService securityService) {
        this.permissionsService = permissionsService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.userGroupDataService = userGroupDataService;
        this.jwtService = jwtService;
        this.securityService = securityService;
    }

    public ResponseEntity<Visas> getPermissionForCurrentUser(Format format) {
        String currentUser = securityService.getCurrentUser().orElseThrow(() -> new ServiceException("Operation not allowed for Anonymous users"));
        String accountId = permissionsService.getAccountByEmail(currentUser).orElseThrow(() -> new ServiceException("Current user is not allowed to access this resource")).getAccountId();
        return getPermissionsForUser(accountId, format);
    }

    @IsAdminReaderOrWriter
    public ResponseEntity<Visas> getPermissionsForUser(String userId, Format format) {
        Visas response = new Visas();

        String userAccountId = getAccountIdForElixirId(userId);
        verifyAccountId(userAccountId);

        String controllerEmail = securityService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Invalid controller"));
        Account controllerAccount = permissionsService.getAccountByEmail(controllerEmail).orElseThrow(() ->
                new AccessDeniedException(("No linked EGA account for email ").concat(controllerEmail)));

        List<Visa> visas = this.permissionsService.getControlledVisas(userAccountId, controllerAccount.getAccountId());

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

    @IsAdminWriter
    public ResponseEntity<PermissionsResponses> createPermissions(String userId,
                                                                  List<Object> body,
                                                                  Format format) {

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
        validateJWTPermissionsDatasetBelongsToDAC(ga4ghVisaV1List);
        return ga4ghVisaV1List
                .stream()
                .map((strVisa) -> {
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
        validatePermissionsDatasetBelongsToDAC(passportVisaObjects);
        return passportVisaObjects
                .stream()
                .map((passportVisaObject) -> handlePassportVisaObjectProcessing(accountId, passportVisaObject))
                .collect(Collectors.toList());
    }

    @IsAdminWriter
    public ResponseEntity<Void> deletePermissions(String accountId, List<String> values) {
        verifyAccountId(accountId);
        if (values.contains("all")) { //ignore all other values and remove all permissions
            values = this.getAllPermissionsForUser(accountId);
        } else {
            values.forEach(this::validateDatasetBelongsToDAC);
        }
        this.permissionsService.deletePassportVisaObject(accountId, values);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private PermissionsResponse handlePassportVisaObjectProcessing(String accountId, PassportVisaObject passportVisaObject) {
        try {
            this.permissionsService.savePassportVisaObject(accountId, passportVisaObject);
            final PermissionsResponse permissionsResponse = getPermissionsResponse(HttpStatus.CREATED, "Created");
            permissionsResponse.setGa4ghVisaV1(passportVisaObject);
            return permissionsResponse;
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


    private void validateJWTPermissionsDatasetBelongsToDAC(List<String> ga4ghVisaV1List) {
        String bearerAccountId = getBearerAccountId();
        if (!userGroupDataService.isEGAAdmin(bearerAccountId)) {
            ga4ghVisaV1List
                    .stream()
                    .filter((strVisa) -> {
                        try {
                            Visa visa = tokenPayloadMapper.mapJWTClaimSetToVisa(SignedJWT.parse(strVisa).getJWTClaimsSet());
                            String datasetId = visa.getGa4ghVisaV1().getValue();
                            return !userGroupDataService.datasetBelongsToDAC(bearerAccountId, datasetId);
                        } catch (ParseException ex) {
                            return true;
                        }
                    })
                    .findAny()
                    .ifPresent(a -> {
                        throw new ValidationException("User doesn't own dataset.");
                    });
        }
    }

    private void validatePermissionsDatasetBelongsToDAC(List<PassportVisaObject> passportVisaObjects) {
        String bearerAccountId = getBearerAccountId();
        if (!userGroupDataService.isEGAAdmin(bearerAccountId)) {
            passportVisaObjects
                    .stream()
                    .filter((passportVisaObject) -> {
                        String datasetId = passportVisaObject.getValue();
                        return !userGroupDataService.datasetBelongsToDAC(bearerAccountId, datasetId);
                    })
                    .findAny()
                    .ifPresent(a -> {
                        throw new ValidationException("User doesn't own dataset.");
                    });
        }
    }

    public void validateDatasetBelongsToDAC(String datasetId) {
        String bearerAccountId = getBearerAccountId();
        if (!userGroupDataService.isEGAAdmin(bearerAccountId)) {
            if (!userGroupDataService.datasetBelongsToDAC(bearerAccountId, datasetId))
                throw new ValidationException("User doesn't own dataset.");
        }
    }

    private List<String> getAllPermissionsForUser(String accountId) {
        return permissionsService.getPermissionByAccountIdAndController(accountId, getBearerAccountId());
    }

    private String getBearerAccountId() {
        String email = securityService.getCurrentUser().orElseThrow(() -> new ValidationException("Anonymous user not allowd."));
        if (email.toLowerCase().endsWith(ELIXIR_ACCOUNT_SUFFIX)) {
            return permissionsService.getAccountIdForElixirId(email).get().getAccountId();
        } else {
            return permissionsService.getAccountByEmail(email).get().getAccountId();
        }
    }

    private SignedJWT createSignedJWT(final Visa visa) {
        //Create JWT token
        final SignedJWT ga4ghSignedJWT = this.jwtService.createJWT(visa);

        //Sign JWT token by signer
        this.jwtService.signJWT(ga4ghSignedJWT);
        return ga4ghSignedJWT;
    }
}
