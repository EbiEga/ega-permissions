package uk.ac.ebi.ega.permissions.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.service.AccessGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

import java.io.Serializable;
import java.util.List;

import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;

public class CustomPermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPermissionEvaluator.class);

    public static final String ELIXIR_ACCOUNT_SUFFIX = "@elixir-europe.org";

    private PermissionsService permissionsService;
    private AccessGroupDataService userGroupDataService;

    public CustomPermissionEvaluator(PermissionsService permissionsService, AccessGroupDataService userGroupDataService) {
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
        LOGGER.debug("Evaluating auth:{}, targetType:{}, permission:{}", auth, targetType, permission);

        String email = auth.getName();

        String accountId = getAccountIdForElixirId(email);
        List<AccessGroup> userGroups = userGroupDataService.getPermissionGroups(accountId);

        if (userGroups.isEmpty()) {
            throw new AccessDeniedException(("No linked user group for ").concat(email));
        }

        return userGroups.stream().anyMatch(entry -> {
            String accessGroup = entry.getGroupType().name();
            String accessLevel = entry.getPermission().name();
            return EGAAdmin.name().equals(accessGroup) || permission.equals(accessGroup.concat("_").concat(accessLevel));
        });
    }

    public String getAccountIdForElixirId(String email) {
        if (email.toLowerCase().endsWith(ELIXIR_ACCOUNT_SUFFIX)) {
            AccountElixirId accountIdForElixirId =
                    permissionsService.getAccountIdForElixirId(email)
                            .orElseThrow(() -> new AccessDeniedException("No linked EGA account for accountId ".concat(email)));
            return accountIdForElixirId.getAccountId();
        } else {
            Account account = permissionsService.getAccountByEmail(email).orElseThrow(() ->
                    new AccessDeniedException(("No linked EGA account for email ").concat(email)));
            return account.getAccountId();
        }
    }
}
