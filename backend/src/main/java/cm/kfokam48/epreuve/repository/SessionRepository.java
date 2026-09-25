package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Accès aux données des sessions.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByCode(String code);
}
