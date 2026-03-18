package ma.karflow.feature.rental.mapper;

import ma.karflow.feature.rental.dto.RentalResponse;
import ma.karflow.feature.rental.entity.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {

    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.licensePlate", target = "vehicleLicensePlate")
    @Mapping(source = "vehicle.vehicleModel.name", target = "vehicleModelName")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientFullName", expression = "java(entity.getClient().getFirstName() + \" \" + entity.getClient().getLastName())")
    @Mapping(source = "insurance.id", target = "insuranceId")
    @Mapping(source = "insurance.name", target = "insuranceName")
    RentalResponse toResponse(Rental entity);
}
