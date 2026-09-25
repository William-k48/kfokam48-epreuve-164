package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de POST /api/exercices (contrat B2 : { sessionId, etudiantId, lien }).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceRequest(

        @NotNull(message = "La session est obligatoire")
        Long sessionId,

        @NotNull(message = "L'étudiant est obligatoire")
        Long etudiantId,

        @NotBlank(message = "Le lien est obligatoire")
        @Size(max = 500, message = "Le lien ne doit pas dépasser 500 caractères")
        String lien) {
}
