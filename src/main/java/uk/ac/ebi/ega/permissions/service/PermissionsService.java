package uk.ac.ebi.ega.permissions.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;

import java.util.Collections;
import java.util.List;

@Service
public class PermissionsService implements PermissionsApiDelegate {
    @Override
    public ResponseEntity<List<PassportVisaObject>> readPermissions(String accountId) {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
