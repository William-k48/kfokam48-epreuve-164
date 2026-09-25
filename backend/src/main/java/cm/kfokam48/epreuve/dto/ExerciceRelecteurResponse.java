package cm.kfokam48.epreuve.dto;

/**
 * Réponse de PATCH /api/exercices/{id}/relecteur (contrat B2 : { id, statut, relecteurId }).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceRelecteurResponse(
        Long id,
        String statut,
        Long relecteurId) {
}
