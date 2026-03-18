package ma.karflow.feature.rental.mapper;

import ma.karflow.feature.rental.dto.InspectionReportRequest;
import ma.karflow.feature.rental.dto.InspectionReportResponse;
import ma.karflow.feature.rental.entity.InspectionReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InspectionReportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "rental", ignore = true)
    @Mapping(target = "agent", ignore = true)
    InspectionReport toEntity(InspectionReportRequest request);

    @Mapping(source = "rental.id", target = "rentalId")
    InspectionReportResponse toResponse(InspectionReport entity);
}
