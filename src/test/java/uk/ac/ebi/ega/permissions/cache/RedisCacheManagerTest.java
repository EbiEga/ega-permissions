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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import uk.ac.ebi.ega.permissions.cache.dto.DatasetDTO;
import uk.ac.ebi.ega.permissions.cache.dto.UserDatasetPermissionDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.ega.permissions.cache.DataConstants.datasetDTO1;
import static uk.ac.ebi.ega.permissions.cache.DataConstants.datasetDTO2;
import static uk.ac.ebi.ega.permissions.cache.DataConstants.userAccountId;
import static uk.ac.ebi.ega.permissions.cache.DataConstants.userDatasetPermissionDTO;

@ExtendWith(MockitoExtension.class)
public class RedisCacheManagerTest {

    @Mock
    private RedisTemplate<String, UserDatasetPermissionDTO> redisTemplate;

    @Mock
    private ValueOperations<String, UserDatasetPermissionDTO> valueOperations;

    private RedisCacheManager redisCacheManager;

    @BeforeEach
    public void beforeEachTest() {
        redisCacheManager = new RedisCacheManager(
                redisTemplate,
                "test-namespace:"
        );
    }

    @Test
    public void addUserDatasetPermission_WhenCallWithValidDataWhereSomeCacheDataExists_ThenOverwritesDataInCache() {
        //Given: mocking stub
        when(valueOperations.get(anyString())).thenReturn(userDatasetPermissionDTO(datasetDTO1()));
        doNothing().when(valueOperations).set(anyString(), any(UserDatasetPermissionDTO.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //Test: method to be tested
        final Set<DatasetDTO> datasetDTOS = redisCacheManager.addUserDatasetPermission(userAccountId(), Set.of(datasetDTO2()));

        //Assertions: assert expected output
        assertThat(datasetDTOS)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        datasetDTO2()
                );

        //Assertions: verify mock interactions
        verify(valueOperations, times(1)).get(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(UserDatasetPermissionDTO.class));
        verifyNoMoreInteractions(valueOperations);

        verify(redisTemplate, times(2)).opsForValue();
        verifyNoMoreInteractions(redisTemplate);
    }

    @Test
    public void addUserDatasetPermission_WhenCallWithValidDataWhereNoCacheDataExists_ThenStoreDataInCache() {
        //Given: mocking stub
        when(valueOperations.get(anyString())).thenReturn(null);
        doNothing().when(valueOperations).set(anyString(), any(UserDatasetPermissionDTO.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //Test: method to be tested
        final Set<DatasetDTO> datasetDTOS = redisCacheManager.addUserDatasetPermission(userAccountId(), Set.of(datasetDTO1()));

        //Assertions: assert expected output
        assertThat(datasetDTOS)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        datasetDTO1()
                );

        //Assertions: verify mock interactions
        verify(valueOperations, times(1)).get(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(UserDatasetPermissionDTO.class));
        verifyNoMoreInteractions(valueOperations);

        verify(redisTemplate, times(2)).opsForValue();
        verifyNoMoreInteractions(redisTemplate);
    }

    @Test
    public void deleteUserDatasetPermission_WhenCallWithValidDataWhereCacheDataExists_DeletesDatasetFromCache() {
        //Given: mocking stub
        when(valueOperations.get(anyString())).thenReturn(userDatasetPermissionDTO(datasetDTO1()));
        doNothing().when(valueOperations).set(anyString(), any(UserDatasetPermissionDTO.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //Given: test existing data mocked stub
        final Set<DatasetDTO> newlyAddedDatasetDTOS = redisCacheManager.addUserDatasetPermission(userAccountId(), Set.of(datasetDTO1()));
        assertThat(newlyAddedDatasetDTOS)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        datasetDTO1()
                );

        //Test: method to be tested
        final Set<DatasetDTO> afterDeletionDatasetDTOS = redisCacheManager.deleteUserDatasetPermission(userAccountId(), Set.of(datasetDTO1()));

        //Assertions: assert expected output
        assertThat(afterDeletionDatasetDTOS).isEmpty();

        //Assertions: verify mock interactions
        verify(valueOperations, times(2)).get(anyString());
        verify(valueOperations, times(2)).set(anyString(), any(UserDatasetPermissionDTO.class));
        verifyNoMoreInteractions(valueOperations);

        verify(redisTemplate, times(4)).opsForValue();
        verifyNoMoreInteractions(redisTemplate);
    }

    @Test
    public void deleteUserDatasetPermission_WhenCallWithValidDataWhereNoCacheDataExists_ExecutesWithoutError() {
        //Given: mocking stub
        when(valueOperations.get(anyString())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //Test: method to be tested
        final Set<DatasetDTO> datasetDTOS = redisCacheManager.deleteUserDatasetPermission(userAccountId(), Set.of(datasetDTO1()));

        //Assertions: assert expected output
        assertThat(datasetDTOS).isEmpty();

        //Assertions: verify mock interactions
        verify(valueOperations, times(1)).get(anyString());
        verify(valueOperations, never()).set(anyString(), any(UserDatasetPermissionDTO.class));
        verifyNoMoreInteractions(valueOperations);

        verify(redisTemplate, times(1)).opsForValue();
        verifyNoMoreInteractions(redisTemplate);
    }
}
