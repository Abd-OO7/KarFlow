package ma.karflow.feature.client.service;

import ma.karflow.feature.client.dto.ClientRequest;
import ma.karflow.feature.client.dto.ClientResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {
    PageResponse<ClientResponse> getAll(Pageable pageable);
    ClientResponse getById(UUID id);
    ClientResponse create(ClientRequest request);
    ClientResponse update(UUID id, ClientRequest request);
    void delete(UUID id);
    PageResponse<ClientResponse> search(String query, Pageable pageable);
}
