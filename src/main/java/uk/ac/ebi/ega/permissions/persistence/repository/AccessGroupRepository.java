package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.AccessGroupId;
import uk.ac.ebi.ega.permissions.persistence.entities.GroupType;

import java.util.List;

public interface AccessGroupRepository extends CrudRepository<AccessGroup, AccessGroupId> {

    String APPROVED = "approved";
    String REVOKED = "revoked";

    @Query("select case when count(ug)> 0 then true else false end from AccessGroup ug where ug.egaAccountStableId=:userId and ug.groupType=:accessGroup")
    boolean existsByUserIdAndAccessGroup(@Param("userId") String userId, @Param("accessGroup") GroupType groupType);

    @Query("select case when count(ug)>0 then true else false end from AccessGroup ug" +
            " inner join Dataset ds on ug.groupStableId = ds.dacStableId" +
            " where ug.egaAccountStableId = :userId and ds.datasetId=:datasetId and ug.status='" + APPROVED + "'")
    boolean userCanControlDataset(@Param("userId") String userId,
                                  @Param("datasetId") String datasetId);

    @Query("select ug from AccessGroup ug inner join PassportClaim pc "
            + "on ug.groupStableId = pc.source "
            + "where ug.egaAccountStableId = :userId and pc.value = :datasetId and ug.status='" + APPROVED + "' and pc.status ='approved'")
    List<AccessGroup> findAllByUserIdAndDataSetId(@Param("userId") String userId,
                                                  @Param("datasetId") String datasetId);

    @Query("select ug from AccessGroup ug where ug.egaAccountStableId=:userId and ug.status='" + APPROVED + "'")
    List<AccessGroup> findAllByUserId(@Param("userId") String userId);

    @Query("select ug from AccessGroup ug inner join Dataset dc "
            + "on ug.groupStableId = dc.dacStableId "
            + "where ug.egaAccountStableId = :bearerAccountId and dc.datasetId = :datasetId and ug.status='" + APPROVED + "'")
    List<AccessGroup> findAllUserDatasetBelongsToDAC(@Param("bearerAccountId") String bearerAccountId,
                                                     @Param("datasetId") String datasetId);

    @Query("select case when (count(ug) > 0)  then true else false end from AccessGroup ug " +
            " where ug.egaAccountStableId=:userAccountId and ug.groupType='EGAAdmin' and ug.status='" + APPROVED + "'")
    boolean isEGAAdmin(@Param("userAccountId") String userAccountId);

    @Query("select new uk.ac.ebi.ega.permissions.model.GroupUserDTO(ac.accountId, ac.email, ug.permission) from AccessGroup ug" +
            " inner join Account ac on ug.egaAccountStableId=ac.accountId" +
            " where ug.groupStableId=:groupId")
    List<GroupUserDTO> findAllUsersByGroup(@Param("groupId") String groupId);


}
