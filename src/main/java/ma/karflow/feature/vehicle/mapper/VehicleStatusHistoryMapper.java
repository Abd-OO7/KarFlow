package ma.karflow.feature.vehicle.mapper;

import ma.karflow.feature.vehicle.dto.VehicleStatusHistoryResponse;
import ma.karflow.feature.vehicle.entity.VehicleStatusHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleStatusHistoryMapper {

    VehicleStatusHistoryResponse toResponse(VehicleStatusHistory entity);
}
