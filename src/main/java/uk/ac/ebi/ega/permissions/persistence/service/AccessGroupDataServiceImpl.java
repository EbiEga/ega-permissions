package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.model.GroupUserDTO;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroupId;
import uk.ac.ebi.ega.permissions.persistence.repository.AccessGroupRepository;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;

public class AccessGroupDataServiceImpl implements AccessGroupDataService {

    private AccessGroupRepository userGroupRepository;

    public AccessGroupDataServiceImpl(AccessGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public boolean isEGAAdmin(String accountId) {
        return userGroupRepository.existsByUserIdAndAccessGroup(accountId, EGAAdmin);
    }

    @Override
    public boolean datasetBelongsToDAC(String bearerAccountId, String datasetId) {
        return !userGroupRepository.findAllUserDatasetBelongsToDAC(bearerAccountId, datasetId).isEmpty();
    }

    @Override
    public List<AccessGroup> getPermissionGroups(String accountId) {
        return userGroupRepository.findAllByUserId(accountId);
    }

    @Override
    public List<GroupUserDTO> getGroupUsers(String groupStableId) {
        return userGroupRepository.findAllUsersByGroup(groupStableId);
    }

    @Override
    public AccessGroup save(AccessGroup userGroup) {
        //Make sure we mark each record we modify as non pea so the migration process handle it properly
        userGroup.setPeaRecord(0);
        return userGroupRepository.save(userGroup);
    }

    @Override
    public Optional<AccessGroup> removeAccessGroup(String accountId, String groupStableId) {
        AccessGroup deletedEntity = null;
        Optional<AccessGroup> optionalAccessGroup = this.userGroupRepository.findById(new AccessGroupId(accountId, groupStableId));
        if (optionalAccessGroup.isPresent()) {
            AccessGroup accessGroup = optionalAccessGroup.get();
            accessGroup.setStatus("revoked");
            deletedEntity = this.userGroupRepository.save(accessGroup);
        }
        return Optional.ofNullable(deletedEntity);
    }
}
