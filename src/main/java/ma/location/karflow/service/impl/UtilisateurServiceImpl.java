package ma.location.karflow.service.impl;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.repository.UtilisateurRepository;
import ma.location.karflow.service.UtilisateurService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
}

