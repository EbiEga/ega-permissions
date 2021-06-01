/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.cache;

import uk.ac.ebi.ega.permissions.cache.dto.DatasetDTO;
import uk.ac.ebi.ega.permissions.cache.dto.UserDatasetPermissionDTO;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DataConstants {

    String EGA_ACCOUNT_ID = "EGAW00000000001";
    String DATASET_ID_1 = "EGAD00001000001";
    String DATASET_ID_2 = "EGAD00001000002";
    String DAC_ID = "EGAC00001000001";
    long ASSERTED_1 = 1618309348;
    long ASSERTED_2 = 1618309457;

    static String userAccountId() {
        return EGA_ACCOUNT_ID;
    }

    static DatasetDTO datasetDTO1() {
        return new DatasetDTO(
                DATASET_ID_1,
                DAC_ID,
                ASSERTED_1
        );
    }

    static DatasetDTO datasetDTO2() {
        return new DatasetDTO(
                DATASET_ID_2,
                DAC_ID,
                ASSERTED_2
        );
    }

    static UserDatasetPermissionDTO userDatasetPermissionDTO(final DatasetDTO datasetDTO) {
        final UserDatasetPermissionDTO userDatasetPermissionDTO = new UserDatasetPermissionDTO();
        userDatasetPermissionDTO.setDatasetDTOS(Stream.of(datasetDTO).collect(Collectors.toSet()));
        return userDatasetPermissionDTO;
    }
}
