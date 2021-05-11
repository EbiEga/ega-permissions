package uk.ac.ebi.ega.permissions.configuration.security.customauthorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission('ALL', 'EGAAdmin_write')")
public @interface HasAdminPermissions {
}
