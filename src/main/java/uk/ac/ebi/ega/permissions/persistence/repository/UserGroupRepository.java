package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroupId;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends CrudRepository<UserGroup, UserGroupId> {

    @Query("select case when count(ug)> 0 then true else false end from UserGroup ug where ug.userId=:userId and ug.accessGroup=:accessGroup")
    boolean existsByUserIdAndAccessGroup(@Param("userId") String userId, @Param("accessGroup") AccessGroup accessGroup);

    @Query("select ug from UserGroup ug inner join PassportClaim pc "
            + "on ug.groupId = pc.source "
            + "where ug.userId = :userId and pc.value = :datasetId and ug.status='approved' and pc.status ='approved'")
    Optional<List<UserGroup>> findAllByUserIdAndDataSetId(@Param("userId") String userId,
                                                          @Param("datasetId") String datasetId);

    @Query("select ug from UserGroup ug where ug.userId=:userId and ug.status='approved'")
    Optional<List<UserGroup>> findAllByUserId(@Param("userId") String userId);

    @Query("select ug from UserGroup ug inner join Dataset dc "
            + "on ug.groupId = dc.dacStableId "
            + "where ug.userId = :bearerAccountId and dc.datasetId = :datasetId and ug.status='approved'")
    Optional<List<UserGroup>> findAllUserDatasetBelongsToDAC(@Param("bearerAccountId") String bearerAccountId,
                                                             @Param("datasetId") String datasetId);
}
