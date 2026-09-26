package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des relectures.
 * v2 (V3) : une relecture par couple (exercice, relecteur) — jusqu'à 2 par
 * exercice ; la note retenue est la moyenne des relectures rendues (A9).
 */
@Repository
public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** Les relectures rendues d'un exercice (0, 1 ou 2). */
    List<Relecture> findByExerciceId(Long exerciceId);

    /** La relecture d'un couple (exercice, relecteur) — RG9 v2. */
    Optional<Relecture> findByExerciceIdAndRelecteurId(Long exerciceId, Long relecteurId);

    List<Relecture> findByExerciceEtudiantId(Long etudiantId);
}
