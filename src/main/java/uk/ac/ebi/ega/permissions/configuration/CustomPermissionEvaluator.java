package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;

import static uk.ac.ebi.ega.permissions.persistence.entities.UserGroup.AccessGroup.EGAAdmin;

import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.List;

public class CustomPermissionEvaluator implements PermissionEvaluator {

    private PermissionsService permissionsService;
    private UserGroupDataService userGroupDataService;

    public CustomPermissionEvaluator(PermissionsService permissionsService, UserGroupDataService userGroupDataService) {
        this.permissionsService = permissionsService;
        this.userGroupDataService = userGroupDataService;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        return hasPrivilege(auth, targetDomainObject.getClass().getSimpleName().toUpperCase(), permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || targetType == null || !(permission instanceof String)) {
            return false;
        }
        return hasPrivilege(auth, targetType.toUpperCase(), permission.toString());
    }

    private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
        String email = auth.getName();
        Account account = permissionsService.getAccountByEmail(email).orElseThrow(() ->
                new ValidationException(("No linked EGA account for email ").concat(email)));

        String accountId = account.getAccountId();
        List<UserGroup> userGroups = userGroupDataService.getPermissionGroups(accountId).orElseThrow(() ->
                new ValidationException(("No linked user group for ").concat(email)));

        return userGroups.stream().anyMatch(entry -> {
            String accessGroup = entry.getAccessGroup().name();
            String accessLevel = entry.getAccessLevel().name();

            if (EGAAdmin.name().equals(accessGroup) || permission.equals(accessGroup.concat("_").concat(accessLevel))) {
                return true;
            }
            return false;
        });
    }
}
