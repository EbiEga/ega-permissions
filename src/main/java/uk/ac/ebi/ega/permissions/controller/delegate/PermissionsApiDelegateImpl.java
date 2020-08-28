package uk.ac.ebi.ega.permissions.controller.delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.util.List;

public class PermissionsApiDelegateImpl implements PermissionsApiDelegate {

    private PermissionsService permissionsService;
    private RequestHandler requestHandler;

    @Autowired
    public PermissionsApiDelegateImpl(PermissionsService permissionsService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseEntity<List<PermissionsResponse>> createPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(requestHandler.createPermissions(accountId, passportVisaObjects));
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        return requestHandler.deletePermissions(accountId, value);
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