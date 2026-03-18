package ma.karflow.feature.vehicle.mapper;

import ma.karflow.feature.vehicle.dto.BrandRequest;
import ma.karflow.feature.vehicle.dto.BrandResponse;
import ma.karflow.feature.vehicle.entity.Brand;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Brand toEntity(BrandRequest request);

    BrandResponse toResponse(Brand entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(BrandRequest request, @MappingTarget Brand entity);
}
