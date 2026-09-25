package cm.kfokam48.epreuve.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Payload de PATCH /api/exercices/{id}/relecteur (décision A2 : réassignation manuelle par le formateur).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ReassignationRelecteurRequest(

        @NotNull(message = "Le relecteur est obligatoire")
        Long relecteurId) {
}
