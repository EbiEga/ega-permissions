package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, String> {
    Optional<Account> findByEmail(String email);
}
