package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;

import java.util.List;

public class PermissionsDataServiceImpl implements PermissionsDataService {

    private PassportClaimRepository passportClaimRepository;

    public PermissionsDataServiceImpl(PassportClaimRepository passportClaimRepository) {
        this.passportClaimRepository = passportClaimRepository;
    }

    @Override
    public List<PassportClaim> getPassPortClaimsForAccount(String accountId) {
        return passportClaimRepository.findAllByAccountId(accountId);
    }

    @Override
    public PassportClaim savePassportClaim(PassportClaim claim) {
        return this.passportClaimRepository.save(claim);
    }

    @Override
    public int deletePassportClaim(String accountId, String value) {
        return this.passportClaimRepository.deleteByAccountIdAndValue(accountId, value);
    }

    @Override
    public boolean accountExists(String accountId) {
        return this.passportClaimRepository.existsPassportClaimByAccountId(accountId);
    }
}
