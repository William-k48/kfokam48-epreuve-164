package cm.kfokam48.epreuve.dto;

/**
 * Une promotion (EF21 : alimente la liste déroulante des promotions).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record PromotionResponse(
        Long id,
        String nom) {
}
