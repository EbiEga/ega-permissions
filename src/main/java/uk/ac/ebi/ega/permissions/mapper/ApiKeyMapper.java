/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.entities.ApiKey;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ApiKeyMapper {

 @Mapping(source = "keyId", target = "id")
 @Mapping(expression = "java(params.getExpiration().getTime())", target = "expirationDate")
 @Mapping(source = "token", target = "token")
 CreatedAPIKey fromApiKeyParams(ApiKeyParams params);

 @Mapping(source = "keyName", target = "id")
 @Mapping(expression = "java(entity.getExpiration().getTime())", target = "expirationDate")
 @Mapping(source = "reason", target = "reason")
 APIKeyListItem fromEntity(ApiKey entity);

 List<APIKeyListItem> fromEntityList(List<ApiKey> apiKeys);
}
