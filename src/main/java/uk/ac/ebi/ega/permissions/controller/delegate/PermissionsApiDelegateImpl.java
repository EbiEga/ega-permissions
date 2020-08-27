package uk.ac.ebi.ega.permissions.controller.delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

public class PermissionsApiDelegateImpl implements PermissionsApiDelegate {

    Logger LOGGER = LoggerFactory.getLogger(PermissionsApiDelegateImpl.class);

    private PermissionsService permissionsService;

    @Autowired
    public PermissionsApiDelegateImpl(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public ResponseEntity<List<PermissionsResponse>> createPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        List<PermissionsResponse> permissionsResponses = new ArrayList<>(passportVisaObjects.size());

        for (PassportVisaObject visaObject : passportVisaObjects) {
            PermissionsResponse permissionsResponse = new PermissionsResponse();
            permissionsResponse.setGa4ghVisaV1(visaObject);

            try {
                this.permissionsService.savePassportVisaObject(accountId, visaObject);
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

            permissionsResponses.add(permissionsResponse);
        }
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(permissionsResponses);
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        verifyAccountId(accountId);
        int permissionsDeleted = this.permissionsService.deletePassportVisaObject(accountId, value);
        if (permissionsDeleted >= 1) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Override
    public ResponseEntity<List<Visa>> readPermissions(String accountId) {
        verifyAccountId(accountId);
        return ResponseEntity.ok(this.permissionsService.getVisas(accountId));
    }

    private void verifyAccountId(String userAccountId) {
        if (!this.permissionsService.accountExist(userAccountId)) {
            throw new ValidationException("User account invalid or not found");
        }
    }
}
