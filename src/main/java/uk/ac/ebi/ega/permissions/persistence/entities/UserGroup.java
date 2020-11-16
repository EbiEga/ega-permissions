package uk.ac.ebi.ega.permissions.persistence.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(UserGroupId.class)
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class UserGroup {
    @Id
    private String userId;

    @Id
    private String groupId;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "access_group")
    private AccessGroup accessGroup;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "access_level")
    private AccessLevel accessLevel;

    private String status;

    private int peaRecord;

    public UserGroup() {
    }

    public UserGroup(String userId, String groupId, AccessGroup accessGroup, AccessLevel accessLevel) {
        this.userId = userId;
        this.groupId = groupId;
        this.accessGroup = accessGroup;
        this.accessLevel = accessLevel;
        this.status = "approved";
        this.peaRecord = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(AccessGroup accessGroup) {
        this.accessGroup = accessGroup;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPeaRecord() {
        return peaRecord;
    }

    public void setPeaRecord(int peaRecord) {
        this.peaRecord = peaRecord;
    }
}
