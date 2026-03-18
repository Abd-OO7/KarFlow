package ma.karflow.feature.claim.service;

import ma.karflow.feature.claim.dto.ClaimRequest;
import ma.karflow.feature.claim.dto.ClaimResponse;
import ma.karflow.feature.claim.dto.ClaimStatusUpdateRequest;
import ma.karflow.feature.claim.enums.ClaimStatus;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClaimService {
    PageResponse<ClaimResponse> getAll(Pageable pageable);
    PageResponse<ClaimResponse> getByStatus(ClaimStatus status, Pageable pageable);
    ClaimResponse getById(UUID id);
    ClaimResponse create(ClaimRequest request);
    ClaimResponse updateStatus(UUID id, ClaimStatusUpdateRequest request);
}
