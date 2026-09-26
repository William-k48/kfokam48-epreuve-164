package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des exercices.
 * v2 (V3) : plus de relecteur_id sur exercice — les relectures en attente
 * d'un relecteur passent par AssignationRelectureRepository.
 */
@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    long countByEtudiantId(Long etudiantId);

    List<Exercice> findByEtudiantId(Long etudiantId);
}
