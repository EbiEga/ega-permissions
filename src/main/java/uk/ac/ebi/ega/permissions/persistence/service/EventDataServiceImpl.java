package uk.ac.ebi.ega.permissions.persistence.service;

import uk.ac.ebi.ega.permissions.persistence.entities.Events;
import uk.ac.ebi.ega.permissions.persistence.repository.EventRepository;

public class EventDataServiceImpl implements EventDataService {
    private EventRepository eventRepository;

    public EventDataServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Events saveEvent(Events events) {
        return eventRepository.save(events);
    }
}
