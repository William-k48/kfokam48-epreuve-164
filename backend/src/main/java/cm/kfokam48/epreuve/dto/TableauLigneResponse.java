package cm.kfokam48.epreuve.dto;

/**
 * Une ligne du tableau récapitulatif du formateur (RG16).
 * moyenne est null si l'étudiant n'a reçu aucune note (décision A7) — jamais 0.
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record TableauLigneResponse(
        Long etudiantId,
        String nom,
        Integer presences,
        Integer exercicesDeposes,
        Double moyenne,
        Integer relecturesEnAttente) {
}
