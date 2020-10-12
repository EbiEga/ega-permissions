package uk.ac.ebi.ega.permissions.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AccountElixirIdRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountElixirIdRepository accountElixirIdRepository;

    @Test
    @DisplayName("Find EGA accountId by ElixirId")
    void findByElixirId() {
        Optional<AccountElixirId> accountElixirIdBeforeInsert = accountElixirIdRepository.findByElixirId("1234@elixir.org");
        accountElixirIdRepository.save(new AccountElixirId("account1", "1234@elixir.org", "account1@abc.pqr"));
        accountElixirIdRepository.save(new AccountElixirId("account2", "15678@elixir.org", "account2@abc.pqr"));

        Optional<AccountElixirId> accountElixirIdAfterInsert = accountElixirIdRepository.findByElixirId("1234@elixir.org");

        assertTrue(accountElixirIdBeforeInsert.isEmpty());
        assertEquals(1, accountElixirIdAfterInsert.stream().count());
    }

}
