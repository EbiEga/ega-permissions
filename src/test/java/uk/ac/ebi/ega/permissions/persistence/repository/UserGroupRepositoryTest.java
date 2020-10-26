package uk.ac.ebi.ega.permissions.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup.EGAAdmin;
import static uk.ac.ebi.ega.permissions.persistence.entities.AccessLevel.read;
import static uk.ac.ebi.ega.permissions.persistence.entities.Authority.dac;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.VisaType;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserGroupRepositoryTest {
    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private PassportClaimRepository passportClaimRepository;

    @Test
    void findAllByUserIdAndDataSetId() {
        Optional<List<UserGroup>> userGroupsBeforeInsert = userGroupRepository.findAllByUserIdAndDataSetId("user1", "d1");
        userGroupRepository.save(new UserGroup("user1", "dac1", EGAAdmin, read));
        userGroupRepository.save(new UserGroup("user2", "dac1", EGAAdmin, read));

        passportClaimRepository.save(new PassportClaim("user1", VisaType.ControlledAccessGrants, 1L, "d1", "dac1", dac));
        passportClaimRepository.save(new PassportClaim("user1", VisaType.ControlledAccessGrants, 1L, "d2", "dac1", dac));
        passportClaimRepository.save(new PassportClaim("user2", VisaType.ControlledAccessGrants, 1L, "d1", "dac1", dac));

        Optional<List<UserGroup>> userGroupsAfterInsert = userGroupRepository.findAllByUserIdAndDataSetId("user1", "d1");

        assertTrue(userGroupsBeforeInsert.isEmpty());
        assertEquals(1, userGroupsAfterInsert.stream().count());
    }
}
