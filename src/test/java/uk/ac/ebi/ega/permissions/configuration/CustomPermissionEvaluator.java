package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.io.Serializable;

@Configuration
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private PermissionsService permissionsService;
    private UserGroupDataService userGroupDataService;

    public CustomPermissionEvaluator(PermissionsService permissionsService, UserGroupDataService userGroupDataService) {
        this.permissionsService = permissionsService;
        this.userGroupDataService = userGroupDataService;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return true;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        return true;
    }

}
