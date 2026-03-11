package ma.location.karflow.service.impl;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.repository.VoitureRepository;
import ma.location.karflow.service.VoitureService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoitureServiceImpl implements VoitureService {

    private final VoitureRepository voitureRepository;
}

