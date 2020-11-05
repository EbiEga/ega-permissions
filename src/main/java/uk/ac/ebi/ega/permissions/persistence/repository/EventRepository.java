package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import uk.ac.ebi.ega.permissions.persistence.entities.Events;

public interface EventRepository extends CrudRepository<Events, Long> {

}
