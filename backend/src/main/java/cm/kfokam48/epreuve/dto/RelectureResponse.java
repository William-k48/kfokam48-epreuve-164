package cm.kfokam48.epreuve.dto;

/**
 * Réponse de POST /api/relectures/{id} (contrat B2 : { id, exerciceId, note, statut }).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record RelectureResponse(
        Long id,
        Long exerciceId,
        Integer note,
        String statut) {
}
