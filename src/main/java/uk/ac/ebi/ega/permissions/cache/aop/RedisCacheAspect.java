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
package uk.ac.ebi.ega.permissions.cache.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.PassportClaim;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.service.PermissionsDataService;
import uk.ac.ebi.ega.permissions.cache.CacheManager;
import uk.ac.ebi.ega.permissions.cache.dto.DatasetDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
public class RedisCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    private final PermissionsDataService permissionsDataService;
    private final CacheManager cacheManager;

    public RedisCacheAspect(final PermissionsDataService permissionsDataService,
                            final CacheManager cacheManager) {
        this.permissionsDataService = permissionsDataService;
        this.cacheManager = cacheManager;
    }

    @AfterReturning(pointcut = "@annotation(uk.ac.ebi.ega.permissions.cache.aop.annotation.UpdateCacheAfterCreatePermission) && args(controllerAccount,userAccountId,..)",
            argNames = "controllerAccount,userAccountId")
    public void saveDatasetInCache(final String controllerAccount, final String userAccountId) {
        final Set<DatasetDTO> datasetDTOS = handleCacheData(userAccountId);
        LOGGER.debug("User {} has {} no. of dataset permissions after create permissions operation", userAccountId, datasetDTOS.size());
    }

    @AfterReturning(pointcut = "@annotation(uk.ac.ebi.ega.permissions.cache.aop.annotation.UpdateCacheAfterDeletePermission) && args(userAccountId,..)",
            argNames = "userAccountId")
    public void deleteDatasetFromCache(final String userAccountId) {
        final Set<DatasetDTO> datasetDTOS = handleCacheData(userAccountId);
        LOGGER.debug("User {} has {} no. of dataset permissions after delete permissions operation", userAccountId, datasetDTOS.size());
    }

    private Set<DatasetDTO> handleCacheData(final String userAccountId) {
        final List<PassportClaim> passportClaims = permissionsDataService.getPassportClaimsForAccount(userAccountId);
        final Set<DatasetDTO> datasetDTOS = passportClaims
                .stream()
                .filter(passportClaim -> "approved".equalsIgnoreCase(passportClaim.getStatus()))
                .map(this::buildDatasetDTO)
                .collect(Collectors.toSet());
        return addDatasetPermissionToCache(userAccountId, datasetDTOS);
    }

    private DatasetDTO buildDatasetDTO(final PassportClaim passportClaim) {
        return new DatasetDTO(
                passportClaim.getPassportClaimId().getValue(),
                passportClaim.getSource(),
                passportClaim.getAsserted()
        );
    }

    private Set<DatasetDTO> addDatasetPermissionToCache(final String userAccountId,
                                                        final Set<DatasetDTO> datasetDTOS) {
        return cacheManager.addUserDatasetPermission(userAccountId, datasetDTOS);
    }
}
