package ma.location.karflow.service.impl;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.repository.LocationRepository;
import ma.location.karflow.service.LocationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
}

