package ma.karflow.feature.client.mapper;

import ma.karflow.feature.client.dto.ClientRequest;
import ma.karflow.feature.client.dto.ClientResponse;
import ma.karflow.feature.client.entity.Client;
import org.mapstruct.*;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    Client toEntity(ClientRequest request);

    @Mapping(target = "licenseValid", expression = "java(isLicenseValid(entity))")
    ClientResponse toResponse(Client entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(ClientRequest request, @MappingTarget Client entity);

    default boolean isLicenseValid(Client entity) {
        return entity.getLicenseExpiry() != null && entity.getLicenseExpiry().isAfter(LocalDate.now());
    }
}
