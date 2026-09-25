package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.SessionRequest;
import cm.kfokam48.epreuve.dto.SessionResponse;
import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.exception.PromotionNotFoundException;
import cm.kfokam48.epreuve.repository.PromotionRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Logique métier des sessions (issue #3) : ouverture d'une session de cours
 * avec code de présence à 6 caractères (EF1) valable 15 minutes (RG1).
 */
@Service
public class SessionService {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGUEUR_CODE = 6;
    private static final int DUREE_VALIDITE_MINUTES = 15;

    private final SessionRepository sessionRepository;
    private final PromotionRepository promotionRepository;

    public SessionService(SessionRepository sessionRepository, PromotionRepository promotionRepository) {
        this.sessionRepository = sessionRepository;
        this.promotionRepository = promotionRepository;
    }

    /**
     * EF1 : le formateur ouvre une session et obtient un code de présence.
     * RG1 : le code expire 15 minutes après l'ouverture.
     */
    public SessionResponse ouvrirSession(SessionRequest request) {
        Promotion promotion = promotionRepository.findById(request.promotionId())
                .orElseThrow(PromotionNotFoundException::new);

        LocalDateTime ouvertureAt = LocalDateTime.now();
        LocalDateTime expirationAt = ouvertureAt.plusMinutes(DUREE_VALIDITE_MINUTES);
        String code = genererCodeUnique();

        Session session = new Session(request.titre(), code, ouvertureAt, expirationAt, false, promotion);
        Session enregistree = sessionRepository.save(session);

        return new SessionResponse(
                enregistree.getId(),
                enregistree.getCode(),
                enregistree.getOuvertureAt(),
                enregistree.getExpirationAt());
    }

    /** Génère un code à 6 caractères (A-Z, 0-9) absent de la base (boucle en cas de collision). */
    private String genererCodeUnique() {
        SecureRandom aleatoire = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
            for (int i = 0; i < LONGUEUR_CODE; i++) {
                sb.append(CARACTERES.charAt(aleatoire.nextInt(CARACTERES.length())));
            }
            code = sb.toString();
        } while (sessionRepository.findByCode(code).isPresent());
        return code;
    }
}
