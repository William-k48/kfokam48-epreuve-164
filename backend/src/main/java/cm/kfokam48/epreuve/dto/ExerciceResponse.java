package cm.kfokam48.epreuve.dto;

/**
 * Réponse de POST /api/exercices (contrat B2 : { id, statut }).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceResponse(
        Long id,
        String statut) {
}
