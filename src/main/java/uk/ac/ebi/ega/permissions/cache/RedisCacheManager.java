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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
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
                                                    final DatasetDTO datasetDTO) {
        try {
            return doAddUserDatasetPermission(userAccountId, datasetDTO);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to add dataset permission: " + e.getMessage(), e);
        }
        return Set.of();
    }

    @Override
    public Set<DatasetDTO> deleteUserDatasetPermission(final String userAccountId,
                                                       final DatasetDTO datasetDTO) {
        try {
            return doDeleteUserDatasetPermission(userAccountId, datasetDTO);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to delete dataset permission: " + e.getMessage(), e);
        }
        return Set.of();
    }

    private Set<DatasetDTO> doAddUserDatasetPermission(final String userAccountId,
                                                       final DatasetDTO datasetDTO) throws JsonProcessingException {
        UserDatasetPermissionDTO userDatasetElixirMappingDetails = getCacheData(userAccountId);
        if (userDatasetElixirMappingDetails != null) {
            if (userDatasetElixirMappingDetails.getDatasetDTOS() == null) {
                userDatasetElixirMappingDetails.setDatasetDTOS(newMutableSet(datasetDTO));
            } else {
                userDatasetElixirMappingDetails
                        .getDatasetDTOS()
                        .add(datasetDTO);
            }
        } else {
            userDatasetElixirMappingDetails = new UserDatasetPermissionDTO();
            userDatasetElixirMappingDetails.setDatasetDTOS(newMutableSet(datasetDTO));
        }
        setCacheValue(userAccountId, userDatasetElixirMappingDetails);
        LOGGER.debug("Dataset permission '{}' has been added to the Cache for user '{}'", datasetDTO.getDatasetId(), userAccountId);
        return userDatasetElixirMappingDetails.getDatasetDTOS();
    }

    private Set<DatasetDTO> doDeleteUserDatasetPermission(final String userAccountId,
                                                          final DatasetDTO datasetDTO) throws JsonProcessingException {
        final UserDatasetPermissionDTO userDatasetPermissionDTO = getCacheData(userAccountId);
        if (userDatasetPermissionDTO != null) {
            final Set<DatasetDTO> datasetDTOS = userDatasetPermissionDTO.getDatasetDTOS();
            if (!isEmpty(datasetDTOS)) {
                final boolean isDatasetRemoved = datasetDTOS.remove(datasetDTO);

                if (isDatasetRemoved) {
                    setCacheValue(userAccountId, userDatasetPermissionDTO);
                    LOGGER.debug("Dataset '{}' has been removed from Cache for user '{}'", datasetDTO.getDatasetId(), userAccountId);
                }
                return datasetDTOS;
            }
        }
        return Set.of();
    }

    private UserDatasetPermissionDTO getCacheData(final String key) {
        return redisTemplate.opsForValue().get(cacheNamespace + key);
    }

    private void setCacheValue(final String key, final UserDatasetPermissionDTO userDatasetPermissionDTO) {
        redisTemplate.opsForValue().set(cacheNamespace + key, userDatasetPermissionDTO);
    }

    private Set<DatasetDTO> newMutableSet(final DatasetDTO datasetDTO) {
        return Stream.of(datasetDTO).collect(toSet());
    }
}
