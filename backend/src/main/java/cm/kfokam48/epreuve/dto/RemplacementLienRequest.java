package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de PATCH /api/exercices/{id} (décision A3 : remplacement du lien, EF8).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record RemplacementLienRequest(

        @NotBlank(message = "Le lien est obligatoire")
        @Size(max = 500, message = "Le lien ne doit pas dépasser 500 caractères")
        String lien) {
}
