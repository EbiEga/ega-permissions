package uk.ac.ebi.ega.permissions.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.ega.permissions.TestApplication;
import uk.ac.ebi.ega.permissions.persistence.entities.Authority;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.VisaType;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {TestApplication.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class PassportClaimRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PassportClaimRepository passportClaimRepository;

    @Test
    @DisplayName("CONTEXT check for injected components")
    void injectedComponentsAreNotNull() {
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(entityManager).isNotNull();
        assertThat(passportClaimRepository).isNotNull();
    }

    @Test
    @DisplayName("LIST PassportClaim objects by AccountName")
    void findAllByAccountId() {
        List<PassportClaim> claimsForUser1Before = passportClaimRepository.findAllByAccountId("account1");
        passportClaimRepository.save(createPassportClaim("account1", "object1"));
        passportClaimRepository.save(createPassportClaim("account1", "object2"));
        passportClaimRepository.save(createPassportClaim("account2", "object1"));

        List<PassportClaim> claimsForUser1After = passportClaimRepository.findAllByAccountId("account1");

        assertThat(claimsForUser1Before).hasSize(0);
        assertThat(claimsForUser1After).hasSize(2);
    }

    @Test
    @DisplayName("BOOLEAN CHECK if there's at least one row for the given accountId")
    void existsPassportClaimByAccountId() {
        boolean existsPassportClaimBefore = passportClaimRepository.existsPassportClaimByAccountId("account1");
        passportClaimRepository.save(createPassportClaim("account1", "object1"));
        boolean existsPassportClaimAfter = passportClaimRepository.existsPassportClaimByAccountId("account1");

        assertThat(existsPassportClaimBefore).isEqualTo(false);
        assertThat(existsPassportClaimAfter).isEqualTo(true);
    }


    @Test
    @DisplayName("SOFT DELETE passport claim")
    void deleteByAccountIdAndValue() {
        passportClaimRepository.save(createPassportClaim("account1", "object1"));
        passportClaimRepository.save(createPassportClaim("account1", "object2"));
        passportClaimRepository.save(createPassportClaim("account1", "object3"));

        List<PassportClaim> claimsBefore = passportClaimRepository.findAllByAccountId("account1");

        assertThat(claimsBefore).hasSize(3);
        assertThat(claimsBefore).filteredOn(e -> e.getValue().equals("object2") && e.getStatus().equals("revoked")).hasSize(0);
        assertThat(claimsBefore).filteredOn(e -> e.getValue().equals("object2") && e.getStatus().equals("approved")).hasSize(1);

        Optional<PassportClaim> claimToSoftDelete = passportClaimRepository.findByAccountIdAndValue("account1", "object2");

        if (claimToSoftDelete.isPresent()) {
            PassportClaim claim = claimToSoftDelete.get();
            claim.setStatus("revoked");
            passportClaimRepository.save(claim);
        }

        List<PassportClaim> claimsAfter = passportClaimRepository.findAllByAccountId("account1");

        assertThat(claimsAfter).hasSize(2);
        assertThat(claimsAfter).filteredOn(e -> e.getValue().equals("object2") && e.getStatus().equals("approved")).hasSize(0);
    }

    private PassportClaim createPassportClaim(String accountId, String value) {
        PassportClaim claim = new PassportClaim(accountId,
                VisaType.ControlledAccessGrants,
                1568814383L,
                value,
                "https://ega-archive.org/dacs/EGAC00001111111",
                Authority.dac);
        return claim;
    }
}