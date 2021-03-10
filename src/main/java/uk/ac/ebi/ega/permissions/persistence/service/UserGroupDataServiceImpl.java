package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.model.GroupUserDTO;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;

public class UserGroupDataServiceImpl implements UserGroupDataService {

    private UserGroupRepository userGroupRepository;

    public UserGroupDataServiceImpl(UserGroupRepository userGroupRepository) {
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
    public Optional<List<AccessGroup>> getPermissionGroups(String accountId) {
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
}
