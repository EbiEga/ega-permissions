package uk.ac.ebi.ega.permissions.persistence.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.validation.constraints.NotBlank;

@Entity
@IdClass(UserGroupId.class)
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class UserGroup {

    public enum AccessGroup {
        EGAAdmin, DAC, User
    }

    public enum AccessLevel {
        read, write
    }

    @Id
    private String sourceAccountId;

    @Id
    private String destinationAccountId;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "group_id")
    private AccessGroup accessGroup;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "level_id")
    private AccessLevel accessLevel;

    public UserGroup() {
    }

    public UserGroup(String sourceAccountId, String destinationAccountId, AccessGroup accessGroup, AccessLevel accessLevel) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.accessGroup = accessGroup;
        this.accessLevel = accessLevel;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
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
}
