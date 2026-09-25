package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Payload de POST /api/sessions/{id}/presences (décision A5 : ajout manuel par le formateur).
 * Record = style des issues #3 et #4 ; aucune annotation JPA dans un DTO (B3).
 */
public record PresenceManuelleRequest(

        @NotNull(message = "L'étudiant est obligatoire")
        Long etudiantId) {
}
