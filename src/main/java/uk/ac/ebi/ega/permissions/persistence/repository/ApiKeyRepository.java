package uk.ac.ebi.ega.permissions.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKeyId;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends CrudRepository<ApiKey, ApiKeyId> {

    List<ApiKey> findAllByUsername(String username);

    void removeAllByUsernameAndKeyName(String username, String keyName);

    Optional<ApiKey> findApiKeyByUsernameAndKeyName(String username, String keyName);
}
