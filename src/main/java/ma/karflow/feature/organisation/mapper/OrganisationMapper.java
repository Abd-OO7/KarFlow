package ma.karflow.feature.organisation.mapper;

import ma.karflow.feature.organisation.dto.OrganisationRequest;
import ma.karflow.feature.organisation.dto.OrganisationResponse;
import ma.karflow.feature.organisation.entity.Organisation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = CityMapper.class)
public interface OrganisationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "cities", ignore = true)
    Organisation toEntity(OrganisationRequest request);

    OrganisationResponse toResponse(Organisation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "cities", ignore = true)
    void updateEntity(OrganisationRequest request, @MappingTarget Organisation entity);
}
