package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.ExerciceLienResponse;
import cm.kfokam48.epreuve.dto.ExerciceRelecteurResponse;
import cm.kfokam48.epreuve.dto.ExerciceRequest;
import cm.kfokam48.epreuve.dto.ExerciceResponse;
import cm.kfokam48.epreuve.dto.ReassignationRelecteurRequest;
import cm.kfokam48.epreuve.dto.RemplacementLienRequest;
import cm.kfokam48.epreuve.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * EF8 (décision A3) — PATCH /api/exercices/{id} : remplacement du lien tant que
     * la relecture n'a pas commencé (RG12). Retourne 200 OK, pas 201.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ExerciceLienResponse> remplacerLien(
            @PathVariable Long id,
            @Valid @RequestBody RemplacementLienRequest request) {
        ExerciceLienResponse response = exerciceService.remplacerLien(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * EF11 (décision A2) — PATCH /api/exercices/{id}/relecteur : réassignation manuelle
     * du relecteur par le formateur. Retourne 200 OK, pas 201.
     */
    @PatchMapping("/{id}/relecteur")
    public ResponseEntity<ExerciceRelecteurResponse> reassignerRelecteur(
            @PathVariable Long id,
            @Valid @RequestBody ReassignationRelecteurRequest request) {
        ExerciceRelecteurResponse response = exerciceService.reassignerRelecteur(id, request);
        return ResponseEntity.ok(response);
    }
}
