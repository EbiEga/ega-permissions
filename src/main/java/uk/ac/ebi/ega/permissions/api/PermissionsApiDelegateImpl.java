package uk.ac.ebi.ega.permissions.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

public class PermissionsApiDelegateImpl implements PermissionsApiDelegate {

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

            PassportVisaObject savedObject = this.permissionsService.savePassportVisaObject(accountId, visaObject);
            permissionsResponse.setGa4ghVisaV1(visaObject);

            if (savedObject == null) {
                permissionsResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            } else {
                permissionsResponse.setStatus(HttpStatus.OK.value());
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
