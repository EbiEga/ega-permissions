package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaimId;

import java.util.List;

public interface PassportClaimRepository extends CrudRepository<PassportClaim, PassportClaimId> {

    List<PassportClaim> findAllByAccountId(String accountId);

    boolean existsPassportClaimByAccountId(String accountId);

    @Transactional
    int deleteByAccountIdAndValue(String accountId, String value);

}
