package cm.kfokam48.epreuve.dto;

/**
 * Un exercice assigné au relecteur et non encore relu (EF18).
 * L'identité de l'auteur n'apparaît JAMAIS dans cette réponse (anonymat du dépôt).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record RelectureEnAttenteResponse(
        Long exerciceId,
        String lien,
        String statut) {
}
