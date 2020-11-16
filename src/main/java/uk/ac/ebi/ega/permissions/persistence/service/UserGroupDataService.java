package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;

import java.util.List;
import java.util.Optional;

public interface UserGroupDataService {

    boolean isEGAAdmin(String accountId);

    boolean datasetBelongsToDAC(String bearerAccountId, String datasetId);

    boolean canControlDataset(String accountId, String datasetId);

    Optional<List<UserGroup>> getPermissionGroups(String accountId);

    UserGroup save(UserGroup userGroup);
}
