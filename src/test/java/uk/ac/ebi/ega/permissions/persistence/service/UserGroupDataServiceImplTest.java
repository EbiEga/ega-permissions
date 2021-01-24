package uk.ac.ebi.ega.permissions.persistence.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.ega.permissions.TestApplication;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;
import static uk.ac.ebi.ega.permissions.persistence.entities.Permission.read;

@DataJpaTest
@ContextConfiguration(classes = {TestApplication.class})
class UserGroupDataServiceImplTest {

    @Autowired
    private UserGroupRepository userGroupRepository;

    private UserGroupDataService userGroupDataService;

    @BeforeEach
    void setup() {
        userGroupDataService = new UserGroupDataServiceImpl(userGroupRepository);
    }

    @Test
    @DisplayName("SAVE user group -- Verify that it's not a PEA Record")
    void save() {
        UserGroup userGroup = new UserGroup("user1", "dac1", EGAAdmin, read);
        userGroup.setPeaRecord(1);
        UserGroup savedWithService = userGroupDataService.save(userGroup);
        assertThat(savedWithService.getPeaRecord()).isEqualTo(0);

        //Verify that saving directly to the repository we get the proper value in case we need to specify the pea_record parameter
        userGroup.setPeaRecord(1);
        UserGroup savedWithRepo = userGroupRepository.save(userGroup);
        assertThat(savedWithRepo.getPeaRecord()).isEqualTo(1);

    }
}
