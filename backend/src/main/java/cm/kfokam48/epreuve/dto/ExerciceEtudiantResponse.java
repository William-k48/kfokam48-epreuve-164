package cm.kfokam48.epreuve.dto;

/**
 * Un exercice de l'étudiant, avec sa note retenue et son commentaire dès qu'au
 * moins une relecture est rendue (EF15).
 * v2 (contrat v1.1, décision A9) : `note` est la moyenne des relectures rendues ;
 * `noteProvisoire` vaut true tant qu'un seul des deux relecteurs a rendu,
 * false quand les deux ont rendu, null tant qu'aucune relecture n'est rendue.
 * RG7 : le relecteur n'apparaît JAMAIS ici (ni id, ni nom) — anonymat garanti.
 * Record = style des issues précédentes ; aucune annotation JPA dans un DTO (B3).
 */
public record ExerciceEtudiantResponse(
        Long id,
        Long sessionId,
        String lien,
        String statut,
        Integer note,
        Boolean noteProvisoire,
        String commentaire) {
}
