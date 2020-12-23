package uk.ac.ebi.ega.permissions.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;

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
