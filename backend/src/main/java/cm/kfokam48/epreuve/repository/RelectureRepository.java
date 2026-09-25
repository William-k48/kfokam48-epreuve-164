package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Accès aux données des relectures.
 */
@Repository
public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);
}
