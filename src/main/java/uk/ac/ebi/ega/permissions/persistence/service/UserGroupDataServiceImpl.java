package uk.ac.ebi.ega.permissions.persistence.service;

import static uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup.EGAAdmin;

import java.util.List;
import java.util.Optional;

import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

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
        return userGroupRepository.findAllUserDatasetBelongsToDAC(bearerAccountId, datasetId).isPresent();
    }

    @Override
    public boolean canControlDataset(String accountId, String datasetId) {
        return userGroupRepository.findAllByUserIdAndDataSetId(accountId, datasetId).isPresent();
    }

    @Override
    public Optional<List<UserGroup>> getPermissionGroups(String accountId) {
        return userGroupRepository.findAllByUserId(accountId);
    }

    @Override
    public UserGroup save(UserGroup userGroup) {
        //Make sure we mark each record we modify as non pea so the migration process handle it properly
        userGroup.setPeaRecord(0);
        return userGroupRepository.save(userGroup);
    }
}
