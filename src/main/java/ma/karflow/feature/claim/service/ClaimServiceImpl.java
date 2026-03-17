package ma.karflow.feature.claim.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.claim.dto.ClaimRequest;
import ma.karflow.feature.claim.dto.ClaimResponse;
import ma.karflow.feature.claim.dto.ClaimStatusUpdateRequest;
import ma.karflow.feature.claim.entity.Claim;
import ma.karflow.feature.claim.enums.ClaimStatus;
import ma.karflow.feature.claim.mapper.ClaimMapper;
import ma.karflow.feature.claim.repository.ClaimRepository;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ClientRepository clientRepository;
    private final RentalRepository rentalRepository;
    private final ClaimMapper claimMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClaimResponse> getAll(Pageable pageable) {
        return PageResponse.from(claimRepository.findByTenantId(TenantContext.getTenantId(), pageable), claimMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClaimResponse> getByStatus(ClaimStatus status, Pageable pageable) {
        return PageResponse.from(claimRepository.findByTenantIdAndStatus(TenantContext.getTenantId(), status, pageable), claimMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getById(UUID id) {
        return claimMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public ClaimResponse create(ClaimRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Client client = clientRepository.findByIdAndTenantId(request.clientId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        Rental rental = null;
        if (request.rentalId() != null) {
            rental = rentalRepository.findByIdAndTenantId(request.rentalId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rental", request.rentalId()));
        }

        Claim claim = new Claim();
        claim.setTenantId(tenantId);
        claim.setSubject(request.subject());
        claim.setDescription(request.description());
        claim.setPriority(request.priority() != null ? request.priority() : ma.karflow.feature.claim.enums.ClaimPriority.MEDIUM);
        claim.setStatus(ClaimStatus.OPEN);
        claim.setClient(client);
        claim.setRental(rental);
        claim = claimRepository.save(claim);

        log.info("Claim created: '{}' for client {} (tenantId: {})", claim.getSubject(), client.getFirstName(), tenantId);
        return claimMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public ClaimResponse updateStatus(UUID id, ClaimStatusUpdateRequest request) {
        Claim claim = findByIdOrThrow(id);
        claim.setStatus(request.status());

        if (request.resolution() != null) {
            claim.setResolution(request.resolution());
        }
        if (request.status() == ClaimStatus.RESOLVED || request.status() == ClaimStatus.CLOSED) {
            claim.setResolvedAt(LocalDateTime.now());
        }

        claim = claimRepository.save(claim);
        log.info("Claim {} status updated to {} (tenantId: {})", id, request.status(), claim.getTenantId());
        return claimMapper.toResponse(claim);
    }

    private Claim findByIdOrThrow(UUID id) {
        return claimRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim", id));
    }
}
