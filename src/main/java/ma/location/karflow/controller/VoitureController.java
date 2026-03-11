package ma.location.karflow.controller;

import lombok.RequiredArgsConstructor;
import ma.location.karflow.service.VoitureService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voitures")
@RequiredArgsConstructor
public class VoitureController {

    private final VoitureService voitureService;
}

