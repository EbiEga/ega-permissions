package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.Format;
import uk.ac.ebi.ega.permissions.model.PermissionsResponses;
import uk.ac.ebi.ega.permissions.model.Visas;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ConstraintViolationException;
import java.util.List;

public class PermissionsApiDelegateImpl implements PermissionsApiDelegate {

    private final PermissionsService permissionsService;
    private final RequestHandler requestHandler;

    public PermissionsApiDelegateImpl(PermissionsService permissionsService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseEntity<Visas> readPermissions(String accountId,
                                                 String xAccountId,
                                                 Format format) {
        return requestHandler.getPermissionsForUser(verifyAccountId(accountId, xAccountId), format);
    }

    @Override
    public ResponseEntity<PermissionsResponses> createPermissions(List<Object> body,
                                                                  String xAccountId,
                                                                  String accountId,
                                                                  Format format) {
        return requestHandler.createPermissions(verifyAccountId(accountId, xAccountId), body, format);
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String value,
                                                  String accountId,
                                                  String xAccountId) {
        return requestHandler.deletePermissions(requestHandler.getAccountIdForElixirId(verifyAccountId(accountId, xAccountId)), value);
    }

    //Check that the request contains either the account-id or x-account-id values
    //Throw a validation exception to be caught by the controller advice otherwise
    private String verifyAccountId(String headerParam, String queryParam) {
        if (headerParam != null && !headerParam.isBlank()) {
            return headerParam;
        } else if (queryParam != null && !queryParam.isBlank()) {
            return queryParam;
        } else {
            throw new ConstraintViolationException("ACCOUNT_ID must be present either in Header (x-account-id) or Query (account-id)", null);
        }
    }

}
