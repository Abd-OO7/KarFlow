package ma.karflow.feature.rental.service;

import ma.karflow.feature.rental.dto.*;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RentalService {
    PageResponse<RentalResponse> getAll(Pageable pageable);
    PageResponse<RentalResponse> getByStatus(RentalStatus status, Pageable pageable);
    PageResponse<RentalResponse> getByClient(UUID clientId, Pageable pageable);
    RentalResponse getById(UUID id);
    RentalResponse create(RentalCreateRequest request);
    InspectionReportResponse addInspection(UUID rentalId, InspectionReportRequest request);
    RentalResponse processReturn(UUID rentalId, ReturnRequest request);
    RentalResponse cancel(UUID rentalId);
}
