package ma.karflow.feature.vehicle.mapper;

import ma.karflow.feature.vehicle.dto.VehicleModelRequest;
import ma.karflow.feature.vehicle.dto.VehicleModelResponse;
import ma.karflow.feature.vehicle.entity.VehicleModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = BrandMapper.class)
public interface VehicleModelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "brand", ignore = true)
    VehicleModel toEntity(VehicleModelRequest request);

    VehicleModelResponse toResponse(VehicleModel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "brand", ignore = true)
    void updateEntity(VehicleModelRequest request, @MappingTarget VehicleModel entity);
}
