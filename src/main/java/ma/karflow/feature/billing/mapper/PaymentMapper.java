package ma.karflow.feature.billing.mapper;

import ma.karflow.feature.billing.dto.PaymentResponse;
import ma.karflow.feature.billing.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    PaymentResponse toResponse(Payment entity);
}
