package ma.karflow.feature.rental.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.dto.InsuranceRequest;
import ma.karflow.feature.rental.dto.InsuranceResponse;
import ma.karflow.feature.rental.entity.Insurance;
import ma.karflow.feature.rental.mapper.InsuranceMapper;
import ma.karflow.feature.rental.repository.InsuranceRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final InsuranceMapper insuranceMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InsuranceResponse> getAll(Pageable pageable) {
        return PageResponse.from(insuranceRepository.findByTenantId(TenantContext.getTenantId(), pageable), insuranceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceResponse getById(UUID id) {
        return insuranceMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public InsuranceResponse create(InsuranceRequest request) {
        Insurance insurance = insuranceMapper.toEntity(request);
        insurance.setTenantId(TenantContext.getTenantId());
        return insuranceMapper.toResponse(insuranceRepository.save(insurance));
    }

    @Override
    @Transactional
    public InsuranceResponse update(UUID id, InsuranceRequest request) {
        Insurance insurance = findByIdOrThrow(id);
        insuranceMapper.updateEntity(request, insurance);
        return insuranceMapper.toResponse(insuranceRepository.save(insurance));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Insurance insurance = findByIdOrThrow(id);
        insurance.setDeleted(true);
        insuranceRepository.save(insurance);
    }

    private Insurance findByIdOrThrow(UUID id) {
        return insuranceRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance", id));
    }
}
