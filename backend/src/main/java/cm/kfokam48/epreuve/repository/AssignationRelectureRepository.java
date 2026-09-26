package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.AssignationRelecture;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.StatutExercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des assignations de relecture (RG5 v2, décision A9) :
 * au plus deux relecteurs distincts par exercice.
 */
@Repository
public interface AssignationRelectureRepository extends JpaRepository<AssignationRelecture, Long> {

    /** Les relecteurs assignés d'un exercice (0, 1 ou 2 lignes). */
    List<AssignationRelecture> findByExerciceId(Long exerciceId);

    /** L'assignation d'un relecteur donné sur un exercice donné. */
    Optional<AssignationRelecture> findByExerciceIdAndRelecteurId(Long exerciceId, Long relecteurId);

    /** Nombre d'assignations d'un exercice (limite de 2 contrôlée en service, RG5 v2). */
    long countByExerciceId(Long exerciceId);

    /** Les exercices qu'un étudiant doit encore relire : il est assigné et l'exercice est EN_ATTENTE. */
    List<Exercice> findDistinctExerciceByRelecteurIdAndExercice_Statut(
            Long relecteurId, StatutExercice statut);
}
