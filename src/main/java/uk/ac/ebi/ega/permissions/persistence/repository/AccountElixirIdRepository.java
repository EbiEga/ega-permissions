package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;

import java.util.Optional;

public interface AccountElixirIdRepository extends CrudRepository<AccountElixirId, String> {

    Optional<AccountElixirId> findByElixirId(String elixirId);

}
