package ma.karflow.feature.vehicle.mapper;

import ma.karflow.feature.vehicle.dto.VehicleRequest;
import ma.karflow.feature.vehicle.dto.VehicleResponse;
import ma.karflow.feature.vehicle.entity.Vehicle;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {VehicleModelMapper.class, CategoryMapper.class})
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "category", ignore = true)
    Vehicle toEntity(VehicleRequest request);

    VehicleResponse toResponse(Vehicle entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "vehicleModel", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntity(VehicleRequest request, @MappingTarget Vehicle entity);
}
