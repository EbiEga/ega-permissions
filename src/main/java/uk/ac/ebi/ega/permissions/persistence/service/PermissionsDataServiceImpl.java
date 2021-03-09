package uk.ac.ebi.ega.permissions.persistence.service;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountElixirIdRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import java.util.List;
import java.util.Optional;

public class PermissionsDataServiceImpl implements PermissionsDataService {

    private PassportClaimRepository passportClaimRepository;
    private AccountRepository accountRepository;
    private AccountElixirIdRepository accountElixirIdRepository;
    private UserGroupRepository userGroupRepository;

    public PermissionsDataServiceImpl(PassportClaimRepository passportClaimRepository,
                                      AccountRepository accountRepository,
                                      AccountElixirIdRepository accountElixirIdRepository,
                                      UserGroupRepository userGroupRepository) {
        this.passportClaimRepository = passportClaimRepository;
        this.accountRepository = accountRepository;
        this.accountElixirIdRepository = accountElixirIdRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public List<PassportClaim> getPassportClaimsForAccount(String accountId) {
        return passportClaimRepository.findAllByAccountId(accountId);
    }

    @Override
    public List<PassportClaim> getPassportClaimsForAccountAndController(String userAccountId, String controllerAccountId) {
        if (userGroupRepository.isEGAAdmin(controllerAccountId)) {
            return getPassportClaimsForAccount(userAccountId);
        } else {
            return passportClaimRepository.findAllByAccountIdAndControllerId(userAccountId, controllerAccountId);
        }
    }

    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public Optional<AccountElixirId> getAccountIdForElixirId(String elixirId) {
        return accountElixirIdRepository.findByElixirId(elixirId);
    }

    @Override
    public PassportClaim savePassportClaim(PassportClaim claim) {
        return this.passportClaimRepository.save(claim);
    }

    @Override
    @Transactional
    public Optional<PassportClaim> deletePassportClaim(String accountId, String value) {
        PassportClaim deletedEntity = null;
        Optional<PassportClaim> optionalPassportClaim = this.passportClaimRepository.findByAccountIdAndValue(accountId, value);
        if (optionalPassportClaim.isPresent()) {
            PassportClaim passportClaim = optionalPassportClaim.get();
            passportClaim.setStatus("revoked");
            deletedEntity = this.passportClaimRepository.save(passportClaim);
        }
        return Optional.ofNullable(deletedEntity);
    }

    @Override
    public boolean accountExists(String accountId) {
        return this.passportClaimRepository.existsPassportClaimByAccountId(accountId);
    }

    @Override
    public List<PassportClaim> getPassportClaimsForDataset(String datasetId) {
        return this.passportClaimRepository.findAllByValue(datasetId);
    }

    @Override
    public List<PassportClaim> getPassportClaimsByUserAndController(String accountId, String egaAccountStableId) {
        if (userGroupRepository.isEGAAdmin(egaAccountStableId)) {
            return this.passportClaimRepository.findAllByAccountId(accountId);
        } else {
            return this.passportClaimRepository.findAllByUserAndController(accountId, egaAccountStableId);
        }
    }

    @Override
    public boolean userCanControlDataset(String controllerAccountId, String value) {
        return userGroupRepository.existsByUserIdAndAccessGroup(controllerAccountId, GroupType.EGAAdmin) || userGroupRepository.userCanControlDataset(controllerAccountId, value);
    }


}
