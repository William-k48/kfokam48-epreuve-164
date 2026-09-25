package cm.kfokam48.epreuve.dto;

/**
 * Un étudiant pour la liste déroulante (EF19 : id + nom, rien de plus).
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record EtudiantResponse(
        Long id,
        String nom) {
}
