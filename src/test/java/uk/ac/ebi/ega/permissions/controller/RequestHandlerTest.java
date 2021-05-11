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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.mapper.AccessGroupMapper;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.JWTPermissionsResponse;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.service.AccessGroupDataService;
import uk.ac.ebi.ega.permissions.persistence.service.EventDataService;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.SecurityService;

import javax.validation.ValidationException;
import java.util.ArrayList;
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

class RequestHandlerTest {
    private PermissionsService permissionsService = mock(PermissionsService.class);
    private TokenPayloadMapper tokenPayloadMapper = mock(TokenPayloadMapper.class);
    private AccessGroupMapper accessGroupMapper = mock(AccessGroupMapper.class);
    private AccessGroupDataService userGroupDataService = mock(AccessGroupDataService.class);
    private JWTService jwtService = mock(JWTService.class);
    private SecurityService securityService = mock(SecurityService.class);

    private RequestHandler requestHandler;

    @Test
    void testCreateJWTPermissions_WhenUserIsEGAAdmin_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        List<JWTPermissionsResponse> permissionsCreated = requestHandler.createJWTPermissions(EMPTY,
                Arrays.asList(getTestToken()));
        assertEquals(1, permissionsCreated.size());
    }

    @Test
    void testCreateJWTPermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        List<JWTPermissionsResponse> permissionsCreated = requestHandler.createJWTPermissions(EMPTY,
                Arrays.asList(getTestToken()));
        assertEquals(1, permissionsCreated.size());
    }

    @Test
    void testCreatePermissions_WhenUserIsEGAAdmin_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        List<PermissionsResponse> permissionsCreated = requestHandler.createPlainPermissions(EMPTY,
                Arrays.asList(createPassportVisaObject()));
        assertEquals(1, permissionsCreated.size());
    }

    @Test
    void testCreatePermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnCreatedObject() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        List<PermissionsResponse> permissionsCreated = requestHandler.createPlainPermissions(EMPTY,
                Arrays.asList(createPassportVisaObject()));
        assertEquals(1, permissionsCreated.size());
    }

    @Test
    void testDeletePermissions_WhenUserIsEGAAdmin_ReturnStatusOK() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(true);

        ResponseEntity<Void> responseEntity = requestHandler.deletePermissions(EMPTY, new ArrayList<>());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeletePermissions_WhenUserIsDACAndDatasetBelongToDac_ReturnStatusOK() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(true);

        ResponseEntity<Void> responseEntity = requestHandler.deletePermissions(EMPTY, new ArrayList<>());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeletePermissions_WhenUserIsDACAndDatasetDoesntBelongToDac_ReturnValidationException() {
        when(userGroupDataService.isEGAAdmin(any())).thenReturn(false);
        when(userGroupDataService.datasetBelongsToDAC(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> {
            requestHandler.deletePermissions(EMPTY, Arrays.asList("CODE"));
        }).isInstanceOf(ValidationException.class);
    }

    @BeforeEach
    private void commonMock() {
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        requestHandler = new RequestHandler(permissionsService, tokenPayloadMapper, accessGroupMapper, userGroupDataService, jwtService, securityService);

        Visa visa = new Visa();
        visa.setGa4ghVisaV1(new PassportVisaObject());
        when(tokenPayloadMapper.mapJWTClaimSetToVisa(any())).thenReturn(visa);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test");
        when(permissionsService.accountExist(any())).thenReturn(true);
        when(permissionsService.getAccountByEmail("test")).thenReturn(Optional.of(new Account()));

        when(securityService.getCurrentUser()).thenReturn(Optional.of("test@ebi.ac.uk"));
        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.of(new Account()));
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
