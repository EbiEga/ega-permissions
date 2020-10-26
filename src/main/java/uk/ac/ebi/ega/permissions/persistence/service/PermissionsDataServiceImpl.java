package uk.ac.ebi.ega.permissions.persistence.service;

import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;
import java.util.Optional;

public class PermissionsDataServiceImpl implements PermissionsDataService {

    private PassportClaimRepository passportClaimRepository;
    private AccountRepository accountRepository;

    public PermissionsDataServiceImpl(PassportClaimRepository passportClaimRepository, AccountRepository accountRepository) {
        this.passportClaimRepository = passportClaimRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<PassportClaim> getPassPortClaimsForAccount(String accountId) {
        return passportClaimRepository.findAllByAccountId(accountId);
    }

    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public PassportClaim savePassportClaim(PassportClaim claim) {
        return this.passportClaimRepository.save(claim);
    }

    @Override
    @Transactional
    public int deletePassportClaim(String accountId, String value) {
        return this.passportClaimRepository.deleteByAccountIdAndValue(accountId, value);
    }

    @Override
    public boolean accountExists(String accountId) {
        return this.passportClaimRepository.existsPassportClaimByAccountId(accountId);
    }

    @Override
    public List<PassportClaim> getPassportClaimsForDataset(String datasetId) {
        return this.passportClaimRepository.findAllByValue(datasetId);
    }
}
