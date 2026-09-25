package cm.kfokam48.epreuve.dto;

import java.time.LocalDateTime;

/**
 * Réponse de POST /api/sessions (contrat B2 : { id, code, ouvertureAt, expirationAt }).
 * Record = style de ErrorResponse ; aucune annotation JPA dans un DTO (B3).
 */
public record SessionResponse(
        Long id,
        String code,
        LocalDateTime ouvertureAt,
        LocalDateTime expirationAt) {
}
