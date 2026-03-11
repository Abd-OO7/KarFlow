package ma.location.karflow.service.impl;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.repository.ClientRepository;
import ma.location.karflow.service.ClientService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
}

