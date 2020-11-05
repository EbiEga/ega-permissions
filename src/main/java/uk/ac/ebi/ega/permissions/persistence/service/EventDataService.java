package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.Events;

public interface EventDataService {
    Events saveEvent(Events events);
}
