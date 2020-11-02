package uk.ac.ebi.ega.permissions.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Dataset {
    @Id
    private String datasetId;
    private String description;
    private String dacStableId;
    private String doubleSignature;
}
