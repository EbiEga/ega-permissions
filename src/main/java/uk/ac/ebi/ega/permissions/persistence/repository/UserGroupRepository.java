package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroupId;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends CrudRepository<UserGroup, UserGroupId> {

    boolean existsBySourceAccountIdAndAccessGroup(String sourceAccountId, UserGroup.AccessGroup accessGroup);

    @Query("SELECT ug from UserGroup ug inner join PassportClaim pc " +
            "on ug.destinationAccountId = pc.accountId " +
            "where ug.sourceAccountId = :sourceAccountId and pc.value = :datasetId")
    Optional<List<UserGroup>> findAllByAccountIdAndDataSetId(@Param("sourceAccountId") String sourceAccountId, @Param("datasetId") String datasetId);

    Optional<List<UserGroup>> findAllBySourceAccountId(String accountId);

    Optional<List<UserGroup>> findAllBySourceAccountIdAndDestinationAccountId(String accountId, String destinationAccountId);
}
