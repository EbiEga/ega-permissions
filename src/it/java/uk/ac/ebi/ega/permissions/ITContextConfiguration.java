package uk.ac.ebi.ega.permissions;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.ega.permissions.dto.TokenParams;
import uk.ac.ebi.ega.permissions.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@ActiveProfiles("integration")
@CucumberContextConfiguration
@SpringBootTest(classes = EgaPermissionsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ITContextConfiguration {

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Autowired
    PassportClaimRepository passportClaimRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenParams tokenParams;

}
