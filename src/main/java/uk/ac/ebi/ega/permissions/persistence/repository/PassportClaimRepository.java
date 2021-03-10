package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaimId;

import java.util.List;
import java.util.Optional;

public interface PassportClaimRepository extends CrudRepository<PassportClaim, PassportClaimId> {

    @Query("select pc from PassportClaim pc where pc.accountId=:accountId and pc.status='approved'")
    List<PassportClaim> findAllByAccountId(@Param("accountId") String accountId);

    @Query("select pc from PassportClaim pc" +
            " inner join Dataset ds on pc.value = ds.datasetId" +
            " inner join UserGroup ug on ds.dacStableId=ug.groupStableId" +
            " where ug.egaAccountStableId=:controllerAccountId and pc.accountId=:userAccountId and pc.status='approved'")
    List<PassportClaim> findAllByAccountIdAndControllerId(@Param("userAccountId") String userAccountId,
                                                          @Param("controllerAccountId") String controllerAccountId);

    @Query("select case when count(pc)> 0 then true else false end from PassportClaim pc " +
            "where pc.accountId=:accountId and pc.status='approved'")
    boolean existsPassportClaimByAccountId(@Param("accountId") String accountId);

    @Query("select pc from PassportClaim pc " +
            "where pc.accountId=:accountId and pc.value=:value and pc.status='approved'")
    Optional<PassportClaim> findByAccountIdAndValue(@Param("accountId") String accountId, @Param("value") String value);

    @Query("select pc from PassportClaim pc where pc.value=:value and pc.status='approved'")
    List<PassportClaim> findAllByValue(@Param("value") String value);

    @Query("select pc from PassportClaim pc" +
            " inner join Dataset ds on ds.datasetId=pc.value" +
            " inner join UserGroup ug on ug.groupStableId=ds.dacStableId" +
            " where pc.accountId=:accountId and ug.egaAccountStableId=:egaAccountStableId")
    List<PassportClaim> findAllByUserAndController(@Param("accountId") String accountId, @Param("egaAccountStableId") String egaAccountStableId);
}
