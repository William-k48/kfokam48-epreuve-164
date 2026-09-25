package cm.kfokam48.epreuve.repository;

import cm.kfokam48.epreuve.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des présences.
 */
@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    Optional<Presence> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Presence> findBySessionId(Long sessionId);
}
