package ma.karflow.feature.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.client.dto.ClientRequest;
import ma.karflow.feature.client.dto.ClientResponse;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.mapper.ClientMapper;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getAll(Pageable pageable) {
        return PageResponse.from(
                clientRepository.findByTenantId(TenantContext.getTenantId(), pageable),
                clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getById(UUID id) {
        return clientMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        if (request.cin() != null && !request.cin().isBlank()
                && clientRepository.existsByCinAndTenantId(request.cin(), tenantId)) {
            throw new DuplicateResourceException("Client", "cin", request.cin());
        }

        validateLicense(request);

        Client client = clientMapper.toEntity(request);
        client.setTenantId(tenantId);
        client = clientRepository.save(client);

        log.info("Client created: {} {} (tenantId: {})", client.getFirstName(), client.getLastName(), tenantId);
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Client client = findByIdOrThrow(id);

        // Check CIN uniqueness if changed
        if (request.cin() != null && !request.cin().isBlank()
                && !request.cin().equals(client.getCin())
                && clientRepository.existsByCinAndTenantId(request.cin(), tenantId)) {
            throw new DuplicateResourceException("Client", "cin", request.cin());
        }

        validateLicense(request);

        clientMapper.updateEntity(request, client);
        client = clientRepository.save(client);

        log.info("Client updated: {} {} (tenantId: {})", client.getFirstName(), client.getLastName(), tenantId);
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Client client = findByIdOrThrow(id);
        client.setDeleted(true);
        clientRepository.save(client);
        log.info("Client soft-deleted: {} {} (tenantId: {})", client.getFirstName(), client.getLastName(), client.getTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> search(String query, Pageable pageable) {
        return PageResponse.from(
                clientRepository.search(TenantContext.getTenantId(), query, pageable),
                clientMapper::toResponse);
    }

    private Client findByIdOrThrow(UUID id) {
        return clientRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private void validateLicense(ClientRequest request) {
        if (request.licenseExpiry() != null && request.licenseExpiry().isBefore(LocalDate.now())) {
            throw new BusinessException("Le permis de conduire est expiré (date : " + request.licenseExpiry() + ")");
        }
    }
}
