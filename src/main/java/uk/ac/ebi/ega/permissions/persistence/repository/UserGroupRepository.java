package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroupId;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends CrudRepository<UserGroup, UserGroupId> {

    String APPROVED = "approved";
    String REVOKED = "revoked";

    @Query("select case when count(ug)> 0 then true else false end from UserGroup ug where ug.egaAccountStableId=:userId and ug.groupType=:accessGroup")
    boolean existsByUserIdAndAccessGroup(@Param("userId") String userId, @Param("accessGroup") GroupType groupType);

    @Query("select ug from UserGroup ug inner join PassportClaim pc "
            + "on ug.groupStableId = pc.source "
            + "where ug.egaAccountStableId = :userId and pc.value = :datasetId and ug.status='" + APPROVED + "' and pc.status ='approved'")
    Optional<List<UserGroup>> findAllByUserIdAndDataSetId(@Param("userId") String userId,
                                                          @Param("datasetId") String datasetId);

    @Query("select ug from UserGroup ug where ug.egaAccountStableId=:userId and ug.status='" + APPROVED + "'")
    Optional<List<UserGroup>> findAllByUserId(@Param("userId") String userId);

    @Query("select ug from UserGroup ug inner join Dataset dc "
            + "on ug.groupStableId = dc.dacStableId "
            + "where ug.egaAccountStableId = :bearerAccountId and dc.datasetId = :datasetId and ug.status='" + APPROVED + "'")
    Optional<List<UserGroup>> findAllUserDatasetBelongsToDAC(@Param("bearerAccountId") String bearerAccountId,
                                                             @Param("datasetId") String datasetId);

    @Query("select case when (count(ug) > 0)  then true else false end from UserGroup ug " +
            " where ug.egaAccountStableId=:userAccountId and ug.groupType='EGAAdmin' and ug.status='" + APPROVED + "'")
    boolean isEGAAdmin(@Param("userAccountId") String userAccountId);
}
