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
import uk.ac.ebi.ega.permissions.persistence.service.PermissionsDataService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionsServiceImplTest {

    private PermissionsDataService permissionsDataService = mock(PermissionsDataService.class);
    private TokenPayloadMapper tokenPayloadMapper = Mappers.getMapper(TokenPayloadMapper.class);
    private VisaInfoProperties visaInfoProperties = mock(VisaInfoProperties.class);

    private PermissionsService permissionsService;

    @BeforeEach
    public void setup() {
        permissionsService = new PermissionsServiceImpl(permissionsDataService, tokenPayloadMapper, visaInfoProperties);
    }

    @Test
    @DisplayName("SERVICE_EXCEPTION Cannot connect to the DB")
    void savePassportVisaObject_serviceException() {
        when(permissionsDataService.savePassportClaim(any())).thenThrow(new CannotCreateTransactionException("Error connecting to the DB"));

        assertThatThrownBy(() -> {
            permissionsService.savePassportVisaObject("id", new PassportVisaObject());
        }).isInstanceOf(ServiceException.class).hasMessageContaining("Error saving permissions to the DB");
    }

    @Test
    @DisplayName("SYSTEM_EXCEPTION Cannot process the permission")
    void savePassportVisaObject_systemException() {
        when(permissionsDataService.savePassportClaim(any())).thenThrow(new RuntimeException("Generic Error"));

        assertThatThrownBy(() -> {
            permissionsService.savePassportVisaObject("id", new PassportVisaObject());
        }).isInstanceOf(SystemException.class).hasMessageContaining("Error processing permissions");
    }
}