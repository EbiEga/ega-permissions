package uk.ac.ebi.ega.permissions.controller.delegate;

import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.api.MeApiDelegate;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.Format;
import uk.ac.ebi.ega.permissions.model.Visas;

public class MeApiDelegateImpl implements MeApiDelegate {

    private final RequestHandler requestHandler;

    public MeApiDelegateImpl(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseEntity<Visas> myPermissions(Format format) {
        return requestHandler.getPermissionForCurrentUser(format);
    }
}
