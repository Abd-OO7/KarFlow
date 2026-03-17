package ma.karflow.feature.organisation.mapper;

import ma.karflow.feature.organisation.dto.CityRequest;
import ma.karflow.feature.organisation.dto.CityResponse;
import ma.karflow.feature.organisation.entity.City;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    City toEntity(CityRequest request);

    CityResponse toResponse(City entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(CityRequest request, @MappingTarget City entity);
}
