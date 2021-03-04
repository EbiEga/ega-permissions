/*
 *
 * Copyright 2020-2021 EMBL - European Bioinformatics Institute
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.transaction.CannotCreateTransactionException;
import uk.ac.ebi.ega.permissions.configuration.VisaInfoProperties;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.TokenPayloadMapper;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.persistence.service.EventDataService;
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionsServiceImplTest {

    private PermissionsDataService permissionsDataService = mock(PermissionsDataService.class);
    private EventDataService eventDataService = mock(EventDataService.class);
    private TokenPayloadMapper tokenPayloadMapper = Mappers.getMapper(TokenPayloadMapper.class);
    private VisaInfoProperties visaInfoProperties = mock(VisaInfoProperties.class);
    private SecurityService securityService = mock(SecurityService.class);
    private PermissionsService permissionsService;

    @BeforeEach
    public void setup() {
        permissionsService = new PermissionsServiceImpl(permissionsDataService, eventDataService, tokenPayloadMapper, visaInfoProperties, securityService);
    }

    @Test
    @DisplayName("SERVICE_EXCEPTION Cannot connect to the DB")
    void savePassportVisaObject_serviceException() {
        when(permissionsDataService.userCanControlDataset(any(), any())).thenReturn(true);
        when(permissionsDataService.savePassportClaim(any())).thenThrow(new CannotCreateTransactionException("Error connecting to the DB"));

        assertThatThrownBy(() -> {
            PassportVisaObject passportVisaObject = new PassportVisaObject();
            passportVisaObject.setBy("dac");
            passportVisaObject.setAsserted(1568814383L);
            passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00002222222");
            passportVisaObject.setType("ControlledAccessGrants");
            passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001111111");

            permissionsService.savePassportVisaObject("id","id", passportVisaObject);
        }).isInstanceOf(ServiceException.class).hasMessageContaining("Error saving permissions to the DB");
    }

    @Test
    @DisplayName("SYSTEM_EXCEPTION Cannot process the permission")
    void savePassportVisaObject_systemException() {
        when(permissionsDataService.savePassportClaim(any())).thenThrow(new RuntimeException("Generic Error"));

        assertThatThrownBy(() -> {
            permissionsService.savePassportVisaObject("id","id", new PassportVisaObject());
        }).isInstanceOf(SystemException.class).hasMessageContaining("Error processing permissions");
    }
}