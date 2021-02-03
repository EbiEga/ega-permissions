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
package uk.ac.ebi.ega.permissions.persistence.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.ac.ebi.ega.permissions.persistence.entities.UserGroup;
import uk.ac.ebi.ega.permissions.persistence.repository.UserGroupRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.ega.permissions.persistence.entities.GroupType.EGAAdmin;
import static uk.ac.ebi.ega.permissions.persistence.entities.Permission.read;

@DataJpaTest
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
