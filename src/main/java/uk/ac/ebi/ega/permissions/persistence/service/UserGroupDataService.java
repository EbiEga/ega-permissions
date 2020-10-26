package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;

import java.util.List;
import java.util.Optional;

public interface UserGroupDataService {

    boolean isEGAAdmin(String accountId);

    boolean belongsToDac(String accountId, String dacAccountId);

    boolean canControlDataset(String accountId, String datasetId);

    Optional<List<UserGroup>> getPermissionGroups(String accountId);
}
