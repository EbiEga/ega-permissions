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

    boolean existsByUserIdAndAccessGroup(String userId, AccessGroup accessGroup);

    @Query("SELECT ug from UserGroup ug inner join PassportClaim pc " 
            + "on ug.groupId = pc.source "
            + "where ug.userId = :userId and pc.value = :datasetId")
    Optional<List<UserGroup>> findAllByUserIdAndDataSetId(@Param("userId") String userId,
            @Param("datasetId") String datasetId);

    Optional<List<UserGroup>> findAllByUserId(String userId);

    Optional<List<UserGroup>> findAllByUserIdAndGroupId(String userId, String groupId);
}
