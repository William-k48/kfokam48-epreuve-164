package cm.kfokam48.epreuve.dto;

/**
 * Réponse de PATCH /api/exercices/{id} (contrat B2 : { id, statut, lien }).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceLienResponse(
        Long id,
        String statut,
        String lien) {
}
