package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de POST /api/sessions (contrat B2 : { titre, promotionId }).
 * Record = style de ErrorResponse ; aucune annotation JPA dans un DTO (B3).
 */
public record SessionRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
        String titre,

        @NotNull(message = "La promotion est obligatoire")
        Long promotionId) {
}
