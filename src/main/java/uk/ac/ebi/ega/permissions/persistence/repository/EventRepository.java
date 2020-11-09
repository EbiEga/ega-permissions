package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import uk.ac.ebi.ega.permissions.persistence.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {

}
