package ma.karflow.feature.rental.service;

import ma.karflow.feature.rental.dto.InsuranceRequest;
import ma.karflow.feature.rental.dto.InsuranceResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InsuranceService {
    PageResponse<InsuranceResponse> getAll(Pageable pageable);
    InsuranceResponse getById(UUID id);
    InsuranceResponse create(InsuranceRequest request);
    InsuranceResponse update(UUID id, InsuranceRequest request);
    void delete(UUID id);
}
