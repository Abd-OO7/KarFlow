package ma.karflow.feature.client.service;

import ma.karflow.feature.client.dto.ClientRequest;
import ma.karflow.feature.client.dto.ClientResponse;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.mapper.ClientMapper;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private final UUID tenantId = UUID.randomUUID();
    private Client testClient;
    private ClientResponse testResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        testClient = new Client();
        testClient.setFirstName("Ahmed");
        testClient.setLastName("Benali");
        testClient.setCin("AB123456");
        testClient.setEmail("ahmed@test.ma");
        testClient.setPhone("0612345678");
        testClient.setLicenseNumber("P-12345");
        testClient.setLicenseExpiry(LocalDate.now().plusYears(2));
        testClient.setTenantId(tenantId);

        testResponse = new ClientResponse(
                UUID.randomUUID(), "Ahmed", "Benali", "ahmed@test.ma", "0612345678",
                "AB123456", null, "P-12345", LocalDate.now().plusYears(2),
                null, null, true, LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldSucceed() {
        ClientRequest request = new ClientRequest("Ahmed", "Benali", "ahmed@test.ma",
                "0612345678", "AB123456", null, "P-12345",
                LocalDate.now().plusYears(2), null, null);

        when(clientRepository.existsByCinAndTenantId("AB123456", tenantId)).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(testClient);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(clientMapper.toResponse(testClient)).thenReturn(testResponse);

        ClientResponse result = clientService.create(request);

        assertEquals("Ahmed", result.firstName());
        assertTrue(result.licenseValid());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void create_withDuplicateCin_shouldThrow() {
        ClientRequest request = new ClientRequest("Ahmed", "Benali", null,
                null, "AB123456", null, null, null, null, null);

        when(clientRepository.existsByCinAndTenantId("AB123456", tenantId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> clientService.create(request));
    }

    @Test
    void create_withExpiredLicense_shouldThrow() {
        ClientRequest request = new ClientRequest("Ahmed", "Benali", null,
                null, null, null, "P-12345",
                LocalDate.now().minusDays(1), null, null);

        assertThrows(BusinessException.class, () -> clientService.create(request));
    }

    @Test
    void search_shouldReturnResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(clientRepository.search(tenantId, "ahmed", pageable))
                .thenReturn(new PageImpl<>(List.of(testClient), pageable, 1));
        when(clientMapper.toResponse(testClient)).thenReturn(testResponse);

        var result = clientService.search("ahmed", pageable);

        assertEquals(1, result.totalElements());
        assertEquals("Ahmed", result.content().get(0).firstName());
    }

    @Test
    void delete_shouldSoftDelete() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findByIdAndTenantId(clientId, tenantId)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        clientService.delete(clientId);

        assertTrue(testClient.isDeleted());
    }

    @Test
    void getById_notFound_shouldThrow() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findByIdAndTenantId(clientId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.getById(clientId));
    }
}
