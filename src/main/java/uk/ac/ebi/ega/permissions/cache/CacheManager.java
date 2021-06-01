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

import java.util.Set;

public interface CacheManager {
    /**
     * Adds dataset permission into the cache
     *
     * @param userAccountId EGA account id
     * @param datasetDTOS Set of DatasetDTO object
     *
     * @return Set of DatasetDTO objects
     */
    Set<DatasetDTO> addUserDatasetPermission(String userAccountId, Set<DatasetDTO> datasetDTOS);

    /**
     * Deletes dataset permission from the cache
     *
     * @param userAccountId EGA account id
     * @param datasetDTOS Set of DatasetDTO object
     *
     * @return Set of DatasetDTO objects
     */
    Set<DatasetDTO> deleteUserDatasetPermission(String userAccountId, Set<DatasetDTO> datasetDTOS);
}
