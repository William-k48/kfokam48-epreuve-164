package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.ExerciceRequest;
import cm.kfokam48.epreuve.dto.ExerciceResponse;
import cm.kfokam48.epreuve.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF7 — POST /api/exercices : l'étudiant dépose le lien de son exercice.
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    @PostMapping
    public ResponseEntity<ExerciceResponse> deposerExercice(@Valid @RequestBody ExerciceRequest request) {
        ExerciceResponse response = exerciceService.deposerExercice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
