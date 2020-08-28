package uk.ac.ebi.ega.permissions.controller;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTTokenResponse;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler {

    Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private PermissionsService permissionsService;
    private TokenPayloadMapper tokenPayloadMapper;

    public RequestHandler(PermissionsService permissionsService, TokenPayloadMapper tokenPayloadMapper) {
        this.permissionsService = permissionsService;
        this.tokenPayloadMapper = tokenPayloadMapper;
    }

    public List<JWTTokenResponse> createJWTPermissions(String accountId, List<String> ga4ghVisaV1List) {
        List<JWTTokenResponse> jwtTokenResponses = new ArrayList<>(ga4ghVisaV1List.size());
        for (String strVisa : ga4ghVisaV1List) {
            JWTTokenResponse response;
            try {
                Visa visa = tokenPayloadMapper.mapJWTClaimSetToVisa(SignedJWT.parse(strVisa).getJWTClaimsSet());
                PermissionsResponse preResponse = handlePassportVisaObjectProcessing(accountId, visa.getGa4ghVisaV1());
                response = new JWTTokenResponse(strVisa, preResponse.getStatus(), preResponse.getMessage());
            } catch (ParseException ex) {
                response = new JWTTokenResponse(strVisa, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error decoding the JWT Token");
            }
            jwtTokenResponses.add(response);
        }

        return jwtTokenResponses;
    }

    public List<PermissionsResponse> createPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        List<PermissionsResponse> permissionsResponses = new ArrayList<>(passportVisaObjects.size());
        for (PassportVisaObject passportVisaObject : passportVisaObjects) {
            PermissionsResponse permissionsResponse = handlePassportVisaObjectProcessing(accountId, passportVisaObject);
            permissionsResponses.add(permissionsResponse);
        }
        return permissionsResponses;
    }

    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        verifyAccountId(accountId);
        int permissionsDeleted = this.permissionsService.deletePassportVisaObject(accountId, value);
        if (permissionsDeleted >= 1) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    private PermissionsResponse handlePassportVisaObjectProcessing(String accountId, PassportVisaObject passportVisaObject) {
        PermissionsResponse permissionsResponse = new PermissionsResponse();
        permissionsResponse.setGa4ghVisaV1(passportVisaObject);

        try {
            this.permissionsService.savePassportVisaObject(accountId, passportVisaObject);
            permissionsResponse.setStatus(HttpStatus.CREATED.value());
            permissionsResponse.setMessage("Created");
        } catch (ServiceException ex) {
            LOGGER.error(ex.getMessage(), ex);
            permissionsResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            permissionsResponse.setMessage(ex.getMessage());
        } catch (SystemException ex) {
            LOGGER.error(ex.getMessage(), ex);
            permissionsResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            permissionsResponse.setMessage(ex.getMessage());
        }
        return permissionsResponse;
    }

    private void verifyAccountId(String userAccountId) {
        if (!this.permissionsService.accountExist(userAccountId)) {
            throw new ValidationException("User account invalid or not found");
        }
    }
}
