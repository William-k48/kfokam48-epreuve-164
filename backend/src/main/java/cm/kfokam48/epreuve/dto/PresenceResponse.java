package cm.kfokam48.epreuve.dto;

/**
 * Réponse de POST /api/presences (contrat B2 : { id, sessionId, etudiantId, source }).
 * Record = style de l'issue #3 ; aucune annotation JPA dans un DTO (B3).
 */
public record PresenceResponse(
        Long id,
        Long sessionId,
        Long etudiantId,
        String source) {
}
