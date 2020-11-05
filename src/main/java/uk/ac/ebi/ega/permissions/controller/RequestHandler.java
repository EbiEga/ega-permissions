package uk.ac.ebi.ega.permissions.controller;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTTokenResponse;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class RequestHandler {

    public static final String EGA_ACCOUNT_ID_PREFIX = "EGAW";
    public static final String ELIXIR_ACCOUNT_SUFFIX = "@elixir-europe.org";
    Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private PermissionsService permissionsService;
    private TokenPayloadMapper tokenPayloadMapper;
    private UserGroupDataService userGroupDataService;

    public RequestHandler(PermissionsService permissionsService, TokenPayloadMapper tokenPayloadMapper,
            UserGroupDataService userGroupDataService) {
        this.permissionsService = permissionsService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.userGroupDataService = userGroupDataService;
    }

    public List<JWTTokenResponse> createJWTPermissions(String accountId, List<String> ga4ghVisaV1List) {
        validateJWTPermissionsDatasetBelongsToDAC(ga4ghVisaV1List);
        return ga4ghVisaV1List
                .stream()
                .map((strVisa) -> {
                    try {
                        Visa visa = tokenPayloadMapper.mapJWTClaimSetToVisa(SignedJWT.parse(strVisa).getJWTClaimsSet());
                        PermissionsResponse preResponse = handlePassportVisaObjectProcessing(accountId, visa.getGa4ghVisaV1());
                        return new JWTTokenResponse(strVisa, preResponse.getStatus(), preResponse.getMessage());
                    } catch (ParseException ex) {
                        return new JWTTokenResponse(strVisa, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error decoding the JWT Token");
                    }
                }).collect(Collectors.toList());
    }

    public List<PermissionsResponse> createPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        validatePermissionsDatasetBelongsToDAC(passportVisaObjects);
        return passportVisaObjects
                .stream()
                .map((passportVisaObject) -> handlePassportVisaObjectProcessing(accountId, passportVisaObject))
                .collect(Collectors.toList());
    }

    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        verifyAccountId(accountId);
        validateDatasetBelongsToDAC(value);
        int permissionsDeleted = this.permissionsService.deletePassportVisaObject(accountId, value);
        if (permissionsDeleted >= 1) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
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
        if(!userGroupDataService.isEGAAdmin(bearerAccountId)) {
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
        if(!userGroupDataService.isEGAAdmin(bearerAccountId)) {
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
        if(!userGroupDataService.isEGAAdmin(bearerAccountId)) {
            if(!userGroupDataService.datasetBelongsToDAC(bearerAccountId, datasetId))
                throw new ValidationException("User doesn't own dataset.");
        }
    }

    private String getBearerAccountId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email.toLowerCase().endsWith(ELIXIR_ACCOUNT_SUFFIX)) {
            return permissionsService.getAccountIdForElixirId(email).get().getAccountId();
        } else {
            return permissionsService.getAccountByEmail(email).get().getAccountId();
        }
    }
}
