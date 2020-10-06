package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.util.List;
import java.util.Optional;

public class UserGroupDataServiceImpl implements UserGroupDataService {

    private UserGroupRepository userGroupRepository;

    public UserGroupDataServiceImpl(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public boolean isEGAAdmin(String accountId) {
        return userGroupRepository.existsBySourceAccountIdAndAccessGroup(accountId, UserGroup.AccessGroup.EGAAdmin);
    }

    @Override
    public boolean belongsToDac(String accountId, String dacAccountId) {
        return userGroupRepository.findAllBySourceAccountIdAndDestinationAccountId(accountId, dacAccountId).isPresent();
    }

    @Override
    public boolean canControlDataset(String accountId, String datasetId) {
        return userGroupRepository.findAllByAccountIdAndDataSetId(accountId, datasetId).isPresent();
    }

    @Override
    public Optional<List<UserGroup>> getPermissionGroups(String accountId) {
        return userGroupRepository.findAllBySourceAccountId(accountId);
    }
}
