package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de POST /api/presences (contrat B2 : { code, etudiantId }).
 * Record = style de l'issue #3 ; aucune annotation JPA dans un DTO (B3).
 */
public record PresenceRequest(

        @NotBlank(message = "Le code est obligatoire")
        @Size(min = 6, max = 6, message = "Le code doit contenir exactement 6 caractères")
        String code,

        @NotNull(message = "L'étudiant est obligatoire")
        Long etudiantId) {
}
