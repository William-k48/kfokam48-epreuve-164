package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Accès aux données des promotions.
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
