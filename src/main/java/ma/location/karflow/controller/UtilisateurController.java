package ma.location.karflow.controller;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.service.UtilisateurService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
}

