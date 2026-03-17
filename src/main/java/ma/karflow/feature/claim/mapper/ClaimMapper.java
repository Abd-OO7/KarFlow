package ma.karflow.feature.claim.mapper;

import ma.karflow.feature.claim.dto.ClaimResponse;
import ma.karflow.feature.claim.entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientFullName", expression = "java(entity.getClient().getFirstName() + \" \" + entity.getClient().getLastName())")
    @Mapping(source = "rental.id", target = "rentalId")
    ClaimResponse toResponse(Claim entity);
}
