package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.model.GroupUserDTO;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;

import java.util.List;
import java.util.Optional;

public interface UserGroupDataService {

    boolean isEGAAdmin(String accountId);

    boolean datasetBelongsToDAC(String bearerAccountId, String datasetId);

    Optional<List<AccessGroup>> getPermissionGroups(String accountId);

    List<GroupUserDTO> getGroupUsers(String groupStableId);

    AccessGroup save(AccessGroup userGroup);
}
