package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.PresenceManuelleRequest;
import cm.kfokam48.epreuve.dto.PresenceResponse;
import cm.kfokam48.epreuve.dto.SessionRequest;
import cm.kfokam48.epreuve.dto.SessionResponse;
import cm.kfokam48.epreuve.service.PresenceService;
import cm.kfokam48.epreuve.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF1 — POST /api/sessions : le formateur ouvre une session et obtient un code.
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final PresenceService presenceService;

    public SessionController(SessionService sessionService, PresenceService presenceService) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> ouvrirSession(@Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.ouvrirSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * EF6 (décision A5) — POST /api/sessions/{id}/presences : ajout manuel d'une présence
     * par le formateur (Q14), sans code et source forcée à FORMATEUR (RG13).
     */
    @PostMapping("/{id}/presences")
    public ResponseEntity<PresenceResponse> ajouterPresenceManuelle(
            @PathVariable Long id,
            @Valid @RequestBody PresenceManuelleRequest request) {
        PresenceResponse response = presenceService.ajouterPresenceManuelle(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
