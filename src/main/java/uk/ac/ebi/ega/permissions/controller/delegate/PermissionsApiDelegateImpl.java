package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.util.List;

public class PermissionsApiDelegateImpl {

    private final PermissionsService permissionsService;
    private final RequestHandler requestHandler;

    public PermissionsApiDelegateImpl(PermissionsService permissionsService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.requestHandler = requestHandler;
    }


    public ResponseEntity<List<PermissionsResponse>> createPermissions(String accountId, List<PassportVisaObject> passportVisaObjects) {
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(requestHandler.createPermissions(requestHandler.getAccountIdForElixirId(accountId), passportVisaObjects));
    }


    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        return requestHandler.deletePermissions(requestHandler.getAccountIdForElixirId(accountId), value);
    }


    public ResponseEntity<List<Visa>> readPermissions(String accountId) {
        accountId = requestHandler.getAccountIdForElixirId(accountId);
        requestHandler.verifyAccountId(accountId);
        return ResponseEntity.ok(this.permissionsService.getVisas(accountId));
    }


    public ResponseEntity<List<AccountAccess>> plainDatasetsDatasetIdUsersGet(String datasetId) {
        requestHandler.validateDatasetBelongsToDAC(datasetId);
        return ResponseEntity.ok(this.permissionsService.getGrantedAccountsForDataset(datasetId));
    }
}
