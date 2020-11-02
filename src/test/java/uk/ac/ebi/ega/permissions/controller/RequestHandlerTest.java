package uk.ac.ebi.ega.permissions.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTTokenResponse;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static joptsimple.internal.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

public class RequestHandlerTest {
    private PermissionsService permissionsService = mock(PermissionsService.class);
    private TokenPayloadMapper tokenPayloadMapper = mock(TokenPayloadMapper.class);
    private UserGroupDataService userGroupDataService = mock(UserGroupDataService.class);
    private RequestHandler requestHandler;

    @Test
    void testCreateJWTPermissions_WhenUserIsEGAAdmin_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        List<JWTTokenResponse> permissionsCreated = requestHandler.createJWTPermissions(EMPTY,
                Arrays.asList(getTestToken()));
        assertEquals(permissionsCreated.size(), 1);
    }

    @Test
    void testCreateJWTPermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        List<JWTTokenResponse> permissionsCreated = requestHandler.createJWTPermissions(EMPTY,
                Arrays.asList(getTestToken()));
        assertEquals(permissionsCreated.size(), 1);
    }

    @Test
    void testCreateJWTPermissions_WhenUserIsDACAndDatasetDoesntBelongToDac_ReturnValidationException() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> {
            requestHandler.createJWTPermissions(EMPTY, Arrays.asList(getTestToken()));
        }).isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreatePermissions_WhenUserIsEGAAdmin_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        List<PermissionsResponse> permissionsCreated = requestHandler.createPermissions(EMPTY,
                Arrays.asList(createPassportVisaObject()));
        assertEquals(permissionsCreated.size(), 1);
    }

    @Test
    void testCreatePermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        List<PermissionsResponse> permissionsCreated = requestHandler.createPermissions(EMPTY,
                Arrays.asList(createPassportVisaObject()));
        assertEquals(permissionsCreated.size(), 1);
    }

    @Test
    void testCreatePermissions_WhenUserIsDACAndDatasetDoesntBelongToDac_ReturnValidationException() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> {
            requestHandler.createPermissions(EMPTY, Arrays.asList(createPassportVisaObject()));
        }).isInstanceOf(ValidationException.class);
    }

    @Test
    void testDeletePermissions_WhenUserIsEGAAdmin_ReturnStatusOK() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        ResponseEntity<Void> responseEntity = requestHandler.deletePermissions(EMPTY, EMPTY);
        assertEquals(responseEntity.getStatusCode(), OK);
    }

    @Test
    void testDeletePermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnStatusOK() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        ResponseEntity<Void> responseEntity = requestHandler.deletePermissions(EMPTY, EMPTY);
        assertEquals(responseEntity.getStatusCode(), OK);
    }

    @Test
    void testDeletePermissions_WhenUserIsDACAndDatasetDoesntBelongToDac_ReturnValidationException() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> {
            requestHandler.deletePermissions(EMPTY, EMPTY);
        }).isInstanceOf(ValidationException.class);
    }

    @BeforeEach
    private void commonMock() {
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        requestHandler = new RequestHandler(permissionsService, tokenPayloadMapper, userGroupDataService);

        Visa visa = new Visa();
        visa.setGa4ghVisaV1(new PassportVisaObject());
        when(tokenPayloadMapper.mapJWTClaimSetToVisa(any())).thenReturn(visa);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test");
        when(permissionsService.accountExist(any())).thenReturn(true);
        when(permissionsService.getAccountByEmail("test")).thenReturn(Optional.of(new Account()));
        when(permissionsService.deletePassportVisaObject(any(), any())).thenReturn(1);
    }

    private String getTestToken() {
        return "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlzcyI6Imh0dHBzOlwvXC9jMmlkLmNvbSIsImlhdCI6MTQxNjE1ODU0MX0"
                + ".eyJhbGciOiJSUzI1NiJ9";
    }

    private PassportVisaObject createPassportVisaObject() {
        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setBy("dac");
        passportVisaObject.setAsserted(1568814383L);
        passportVisaObject.setValue("DATASET1");
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setSource("EGAC00001111111");
        return passportVisaObject;
    }

}
