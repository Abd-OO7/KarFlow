package ma.karflow.feature.billing.mapper;

import ma.karflow.feature.billing.dto.InvoiceLineResponse;
import ma.karflow.feature.billing.dto.InvoiceResponse;
import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.entity.InvoiceLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(target = "clientFullName", expression = "java(entity.getRental().getClient().getFirstName() + \" \" + entity.getRental().getClient().getLastName())")
    @Mapping(source = "rental.vehicle.licensePlate", target = "vehicleLicensePlate")
    @Mapping(target = "totalPaid", expression = "java(entity.getTotalPaid())")
    @Mapping(target = "remainingAmount", expression = "java(entity.getTotalAmount() - entity.getTotalPaid())")
    InvoiceResponse toResponse(Invoice entity);

    InvoiceLineResponse toLineResponse(InvoiceLine line);
}
