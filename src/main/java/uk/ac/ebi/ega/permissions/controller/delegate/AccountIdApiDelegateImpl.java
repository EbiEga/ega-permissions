package uk.ac.ebi.ega.permissions.controller.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.AccountIdApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.*;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.util.List;
import java.util.stream.Collectors;

public class AccountIdApiDelegateImpl implements AccountIdApiDelegate {

    private final PermissionsService permissionsService;
    private final RequestHandler requestHandler;

    public AccountIdApiDelegateImpl(PermissionsService permissionsService, RequestHandler requestHandler) {
        this.permissionsService = permissionsService;
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseEntity<Visas> readPermissions(String accountId, Format format) {
        return requestHandler.getPermissionsForUser(accountId, format);
    }

    @Override
    public ResponseEntity<PermissionsResponses> createPermissions(String accountId,
                                                                  List<Object> body,
                                                                  Format format) {

        return requestHandler.createPermissions(accountId, body, format);
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        return requestHandler.deletePermissions(requestHandler.getAccountIdForElixirId(accountId), value);
    }


}
