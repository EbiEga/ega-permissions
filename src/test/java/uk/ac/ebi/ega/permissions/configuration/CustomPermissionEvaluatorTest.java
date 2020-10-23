package uk.ac.ebi.ega.permissions.configuration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessLevel;
import uk.ac.ebi.ega.permissions.persistence.service.UserGroupDataService;
import uk.ac.ebi.ega.permissions.service.PermissionsService;

public class CustomPermissionEvaluatorTest {
    private static final String EGA_ACCOUNT_ID = "EGAW001";
    private PermissionsService permissionsService = mock(PermissionsService.class);
    private UserGroupDataService userGroupDataService = mock(UserGroupDataService.class);
    private Authentication authentication = mock(Authentication.class);

    private CustomPermissionEvaluator customPermissionEvaluator;

    @BeforeEach
    public void setup() {
        when(authentication.getName()).thenReturn(EGA_ACCOUNT_ID);
        customPermissionEvaluator = new CustomPermissionEvaluator(permissionsService, userGroupDataService);
    }

    @Test
    void hasPermission_WhenUserIsEGAAdminAndPermissionRequireEGAAdmin_ReturnTrue() {
        final Account account = new Account(EGA_ACCOUNT_ID, null, null, "email", null);
        final List<UserGroup> userGroups = new ArrayList<>();
        final UserGroup userGroupRead = new UserGroup("sourceAccountId", "destinationAccountId", AccessGroup.EGAAdmin,
                AccessLevel.read);
        userGroups.add(userGroupRead);

        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.of(account));
        when(userGroupDataService.getPermissionGroups(account.getAccountId())).thenReturn(Optional.of(userGroups));

        // EGAAdmin has all the permissions
        assertTrue(customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "EGAAdmin_read"));
        assertTrue(customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "EGAAdmin_write"));
    }

    @Test
    void hasPermission_WhenUserIsDACReadWriteAndPermissionRequireDACReadWrite_ReturnTrue() {
        final Account account = new Account(EGA_ACCOUNT_ID, null, null, "email", null);
        final List<UserGroup> userGroups = new ArrayList<>();
        final UserGroup userGroupRead = new UserGroup("sourceAccountId", "destinationAccountId", AccessGroup.DAC,
                AccessLevel.read);
        final UserGroup userGroupWrite = new UserGroup("sourceAccountId", "destinationAccountId", AccessGroup.DAC,
                AccessLevel.write);
        userGroups.add(userGroupRead);
        userGroups.add(userGroupWrite);

        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.of(account));
        when(userGroupDataService.getPermissionGroups(account.getAccountId())).thenReturn(Optional.of(userGroups));

        assertTrue(customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "DAC_read"));
        assertTrue(customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "DAC_write"));
    }

    @Test
    void hasPermission_WhenUserIsOnlyDACReadAndPermissionRequireDACWrite_ReturnFalse() {
        final Account account = new Account(EGA_ACCOUNT_ID, null, null, "email", null);
        final List<UserGroup> userGroups = new ArrayList<>();
        final UserGroup userGroupRead = new UserGroup("sourceAccountId", "destinationAccountId", AccessGroup.DAC,
                AccessLevel.read);
        userGroups.add(userGroupRead);

        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.of(account));
        when(userGroupDataService.getPermissionGroups(account.getAccountId())).thenReturn(Optional.of(userGroups));

        assertFalse(customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "DAC_write"));
    }

    @Test
    void hasPermission_WhenUserHasNoAccountMapping_ReturnValidationException() {
        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> {
            customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "EGAAdmin_read");
        }).isInstanceOf(ValidationException.class);
    }

    @Test
    void hasPermission_WhenUserHasNoUserGroupMapping_ReturnValidationException() {
        final Account account = new Account(EGA_ACCOUNT_ID, null, null, "email", null);

        when(permissionsService.getAccountByEmail(any())).thenReturn(Optional.of(account));
        when(userGroupDataService.getPermissionGroups(account.getAccountId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            customPermissionEvaluator.hasPermission(authentication, EGA_ACCOUNT_ID, "EGAAdmin_read");
        }).isInstanceOf(ValidationException.class);
    }
}
