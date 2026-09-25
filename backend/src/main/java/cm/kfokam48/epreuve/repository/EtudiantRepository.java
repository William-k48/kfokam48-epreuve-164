package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Accès aux données des étudiants.
 */
@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByPromotionId(Long promotionId);
}
