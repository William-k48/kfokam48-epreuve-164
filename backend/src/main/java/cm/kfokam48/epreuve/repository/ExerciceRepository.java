package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Accès aux données des exercices.
 */
@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
