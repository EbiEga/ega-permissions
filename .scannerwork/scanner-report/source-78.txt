package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.DatasetsApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.util.List;

public class DatasetsApiDelegateImpl implements DatasetsApiDelegate {

    private final RequestHandler requestHandler;
    private final PermissionsService permissionsService;

    public DatasetsApiDelegateImpl(final PermissionsService permissionsService, final RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseEntity<List<AccountAccess>> usersWithAccessToDataset(String datasetId) {
        requestHandler.validateDatasetBelongsToDAC(datasetId);
        return ResponseEntity.ok(this.permissionsService.getGrantedAccountsForDataset(datasetId));
    }
}
