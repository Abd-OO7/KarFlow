package ma.karflow.feature.rental.mapper;

import ma.karflow.feature.rental.dto.InsuranceRequest;
import ma.karflow.feature.rental.dto.InsuranceResponse;
import ma.karflow.feature.rental.entity.Insurance;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InsuranceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Insurance toEntity(InsuranceRequest request);

    InsuranceResponse toResponse(Insurance entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(InsuranceRequest request, @MappingTarget Insurance entity);
}
