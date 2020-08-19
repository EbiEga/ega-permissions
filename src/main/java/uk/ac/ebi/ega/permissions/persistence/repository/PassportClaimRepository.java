package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaimId;

import java.util.List;

public interface PassportClaimRepository extends CrudRepository<PassportClaim, PassportClaimId> {

    List<PassportClaim> findAllByAccountId(String accountId);

    boolean existsPassportClaimByAccountId(String accountId);

    int deleteByAccountIdAndValue(String accountId, String value);

}
