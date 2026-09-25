package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.SessionRequest;
import cm.kfokam48.epreuve.dto.SessionResponse;
import cm.kfokam48.epreuve.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> ouvrirSession(@Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.ouvrirSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
