package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.PresenceRequest;
import cm.kfokam48.epreuve.dto.PresenceResponse;
import cm.kfokam48.epreuve.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF2 — POST /api/presences : l'étudiant marque sa présence avec le code.
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping
    public ResponseEntity<PresenceResponse> marquerPresence(@Valid @RequestBody PresenceRequest request) {
        PresenceResponse response = presenceService.marquerPresence(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
