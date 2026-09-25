package cm.kfokam48.epreuve.dto;

/**
 * Un exercice de l'étudiant, avec sa note et son commentaire si relu (EF15).
 * RG7 : le relecteur n'apparaît JAMAIS ici (ni id, ni nom) — anonymat garanti.
 * note et commentaire sont null tant que l'exercice n'est pas relu.
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceEtudiantResponse(
        Long id,
        Long sessionId,
        String lien,
        String statut,
        Integer note,
        String commentaire) {
}
