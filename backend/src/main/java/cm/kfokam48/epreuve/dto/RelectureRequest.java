package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de POST /api/relectures/{id} (contrat B2 : { note, commentaire }).
 * RG8 : note entière de 0 à 20 (type Integer + @Min/@Max : une note non entière
 * est rejetée par la désérialisation Jackson, hors bornes par la validation).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record RelectureRequest(

        @NotNull(message = "La note est obligatoire")
        @Min(value = 0, message = "La note doit être comprise entre 0 et 20")
        @Max(value = 20, message = "La note doit être comprise entre 0 et 20")
        Integer note,

        @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
        String commentaire) {
}
