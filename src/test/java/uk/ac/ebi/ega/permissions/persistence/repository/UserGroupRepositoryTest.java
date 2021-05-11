/*
 *
 * Copyright 2020-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.persistence.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.ega.permissions.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.permissions.persistence.entities.AccessGroup;
import uk.ac.ebi.ega.permissions.persistence.entities.VisaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.ega.permissions.persistence.entities.Authority.dac;
import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;
import static uk.ac.ebi.ega.permissions.persistence.entities.Permission.read;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class UserGroupRepositoryTest {
    @Autowired
    private AccessGroupRepository userGroupRepository;

    @Autowired
    private PassportClaimRepository passportClaimRepository;

    @Test
    void findAllByUserIdAndDataSetId() {
        List<AccessGroup> userGroupsBeforeInsert = userGroupRepository.findAllByUserIdAndDataSetId("user1", "d1");
        userGroupRepository.save(new AccessGroup("user1", "dac1", EGAAdmin, read));
        userGroupRepository.save(new AccessGroup("user2", "dac1", EGAAdmin, read));

        passportClaimRepository.save(new PassportClaim("user1", VisaType.ControlledAccessGrants, 1L, "d1", "dac1", dac));
        passportClaimRepository.save(new PassportClaim("user1", VisaType.ControlledAccessGrants, 1L, "d2", "dac1", dac));
        passportClaimRepository.save(new PassportClaim("user2", VisaType.ControlledAccessGrants, 1L, "d1", "dac1", dac));

        List<AccessGroup> userGroupsAfterInsert = userGroupRepository.findAllByUserIdAndDataSetId("user1", "d1");

        assertThat(userGroupsBeforeInsert).isEmpty();
        assertEquals(2, userGroupsAfterInsert.size());
    }
}
