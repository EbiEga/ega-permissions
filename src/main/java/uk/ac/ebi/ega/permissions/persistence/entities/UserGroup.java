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
    private String egaAccountStableId;

    @Id
    private String groupStableId;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "group_type")
    private GroupType groupType;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "permission")
    private Permission permission;

    private String status;

    private int peaRecord;

    public UserGroup() {
    }

    public UserGroup(String egaAccountStableId, String groupStableId, GroupType groupType, Permission permission) {
        this.egaAccountStableId = egaAccountStableId;
        this.groupStableId = groupStableId;
        this.groupType = groupType;
        this.permission = permission;
        this.status = "approved";
        this.peaRecord = 0;
    }

    public String getEgaAccountStableId() {
        return egaAccountStableId;
    }

    public void setEgaAccountStableId(String userId) {
        this.egaAccountStableId = userId;
    }

    public String getGroupStableId() {
        return groupStableId;
    }

    public void setGroupStableId(String groupId) {
        this.groupStableId = groupId;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
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
