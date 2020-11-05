package uk.ac.ebi.ega.permissions.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;
    
    @NotNull
    @Size(max = 255)
    private String bearerId;
    
    @NotNull
    @Size(max = 255)
    private String userId;
    
    @NotNull
    @Size(max = 7)
    private String method;

    private String data;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getBearerId() {
        return bearerId;
    }

    public void setBearerId(String bearerId) {
        this.bearerId = bearerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    
}
