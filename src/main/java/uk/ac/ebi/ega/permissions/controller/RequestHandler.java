package uk.ac.ebi.ega.permissions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.*;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestHandler {

    public static final String EGA_ACCOUNT_ID_PREFIX = "EGAW";
    public static final String ELIXIR_ACCOUNT_SUFFIX = "@elixir-europe.org";
    Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private final PermissionsService permissionsService;
    private final TokenPayloadMapper tokenPayloadMapper;
    private final UserGroupDataService userGroupDataService;
    private final JWTService jwtService;

    public RequestHandler(final PermissionsService permissionsService,
                          final TokenPayloadMapper tokenPayloadMapper,
                          final UserGroupDataService userGroupDataService,
                          final JWTService jwtService) {
        this.permissionsService = permissionsService;
        this.tokenPayloadMapper = tokenPayloadMapper;
        this.userGroupDataService = userGroupDataService;
        this.jwtService = jwtService;
    }

    public ResponseEntity<Visas> getPermissionsForUser(String accountId, Format format) {
        Visas response = new Visas();

        if (format == null) {
            format = Format.JWT;
        }

        accountId = getAccountIdForElixirId(accountId);
        verifyAccountId(accountId);
        List<Visa> visas = this.permissionsService.getVisas(accountId);

        if (format == Format.JWT) {
            response.addAll(
                    visas.stream()
                            .map(this::createSignedJWT)
                            .map(JWSObject::serialize)
                            .map(e -> {
                                JWTVisa jwtVisa = new JWTVisa();
                                jwtVisa.setJwt(e);
                                return jwtVisa;
                            })
                            .collect(Collectors.toList())
            );
        } else if (format == Format.PLAIN) {
            response.addAll(visas);
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<PermissionsResponses> createPermissions(String accountId,
                                                                  List<Object> body,
                                                                  Format format) {

        PermissionsResponses responses = new PermissionsResponses();
        List<PassportVisaObject> passportVisaObjects;
        List<String> passportVisaStrings;

        if (format == null) {
            format = Format.JWT;
        }

        ObjectMapper mapper = new ObjectMapper();

        //TODO: Handle mapper exception and improve conversion
        if (format == Format.PLAIN) {
            passportVisaObjects = body.parallelStream().map(v -> mapper.convertValue(v, PassportVisaObject.class)).collect(Collectors.toList());
            List<PermissionsResponse> permissionResponses = createPlainPermissions(getAccountIdForElixirId(accountId), passportVisaObjects);
            responses.addAll(permissionResponses);

        } else if (format == Format.JWT) {
            passportVisaStrings = body.parallelStream().map(v -> mapper.convertValue(v, JWTPassportVisaObject.class).getJwt()).collect(Collectors.toList());
            List<JWTPermissionsResponse> jwtResponses = createJWTPermissions(getAccountIdForElixirId(accountId), passportVisaStrings);
            responses.addAll(jwtResponses);
        }

        return ResponseEntity.ok(responses);
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

    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        verifyAccountId(accountId);
        validateDatasetBelongsToDAC(value);
        this.permissionsService.deletePassportVisaObject(accountId, value);
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

    public Optional<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    private String getBearerAccountId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
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
