package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.Event;

public interface EventDataService {
    Event saveEvent(Event events);
}
