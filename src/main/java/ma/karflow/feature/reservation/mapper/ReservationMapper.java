package ma.karflow.feature.reservation.mapper;

import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientFullName", expression = "java(entity.getClient().getFirstName() + \" \" + entity.getClient().getLastName())")
    @Mapping(source = "client.email", target = "clientEmail")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(target = "vehicleInfo", expression = "java(entity.getVehicle().getVehicleModel().getName() + \" — \" + entity.getVehicle().getLicensePlate())")
    @Mapping(source = "insurance.id", target = "insuranceId")
    @Mapping(source = "insurance.name", target = "insuranceName")
    @Mapping(source = "rental.id", target = "rentalId")
    ReservationResponse toResponse(Reservation entity);
}
