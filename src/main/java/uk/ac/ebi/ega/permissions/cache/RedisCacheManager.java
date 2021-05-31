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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import uk.ac.ebi.ega.permissions.cache.dto.DatasetDTO;
import uk.ac.ebi.ega.permissions.cache.dto.UserDatasetPermissionDTO;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

public class RedisCacheManager implements CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);

    private final RedisTemplate<String, UserDatasetPermissionDTO> redisTemplate;
    private final String cacheNamespace;

    public RedisCacheManager(final RedisTemplate<String, UserDatasetPermissionDTO> redisTemplate,
                             final String cacheNamespace) {
        this.redisTemplate = redisTemplate;
        this.cacheNamespace = cacheNamespace;
    }

    @Override
    public Set<DatasetDTO> addUserDatasetPermission(final String userAccountId,
                                                    final Set<DatasetDTO> datasetDTOS) {
        try {
            return doAddUserDatasetPermission(userAccountId, datasetDTOS);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to add dataset permission: " + e.getMessage(), e);
        }
        return Set.of();
    }

    @Override
    public Set<DatasetDTO> deleteUserDatasetPermission(final String userAccountId,
                                                       final Set<DatasetDTO> datasetDTOS) {
        try {
            return doDeleteUserDatasetPermission(userAccountId, datasetDTOS);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to delete dataset permission: " + e.getMessage(), e);
        }
        return Set.of();
    }

    private Set<DatasetDTO> doAddUserDatasetPermission(final String userAccountId,
                                                       final Set<DatasetDTO> datasetDTOS) throws JsonProcessingException {
        final UserDatasetPermissionDTO userDatasetElixirMappingDetails = getCacheData(userAccountId);
        userDatasetElixirMappingDetails.setDatasetDTOS(datasetDTOS);
        setCacheValue(userAccountId, userDatasetElixirMappingDetails);
        LOGGER.debug("Total '{}' dataset permission has been added to the Cache for user '{}'", datasetDTOS.size(), userAccountId);
        return userDatasetElixirMappingDetails.getDatasetDTOS();
    }

    private Set<DatasetDTO> doDeleteUserDatasetPermission(final String userAccountId,
                                                          final Set<DatasetDTO> datasetDTOS) throws JsonProcessingException {
        final UserDatasetPermissionDTO userDatasetPermissionDTO = getCacheData(userAccountId);
        final Set<DatasetDTO> cacheDatasetDTOS = userDatasetPermissionDTO.getDatasetDTOS();
        if (!isEmpty(cacheDatasetDTOS)) {
            final Set<DatasetDTO> datasetDTOAfterDeletion = cacheDatasetDTOS
                    .stream()
                    .filter(datasetDTO -> !datasetDTOS.contains(datasetDTO))
                    .collect(Collectors.toSet());
            userDatasetPermissionDTO.setDatasetDTOS(datasetDTOAfterDeletion);
            setCacheValue(userAccountId, userDatasetPermissionDTO);
            LOGGER.debug("Total '{}' Dataset have been removed from Cache for user '{}'", datasetDTOAfterDeletion.size(), userAccountId);
            return datasetDTOAfterDeletion;
        }
        return Set.of();
    }

    private UserDatasetPermissionDTO getCacheData(final String key) {
        final UserDatasetPermissionDTO userDatasetPermissionDTO = redisTemplate.opsForValue().get(cacheNamespace + key);
        return userDatasetPermissionDTO == null ? new UserDatasetPermissionDTO() : userDatasetPermissionDTO;
    }

    private void setCacheValue(final String key, final UserDatasetPermissionDTO userDatasetPermissionDTO) {
        redisTemplate.opsForValue().set(cacheNamespace + key, userDatasetPermissionDTO);
    }
}
