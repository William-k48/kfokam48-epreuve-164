package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.ExerciceLienResponse;
import cm.kfokam48.epreuve.dto.ExerciceRequest;
import cm.kfokam48.epreuve.dto.ExerciceResponse;
import cm.kfokam48.epreuve.dto.RemplacementLienRequest;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Presence;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.ExerciceDejaDeposeException;
import cm.kfokam48.epreuve.exception.ExerciceNotFoundException;
import cm.kfokam48.epreuve.exception.LienInvalideException;
import cm.kfokam48.epreuve.exception.RelectureDejaCommenceeException;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.exception.SessionNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF7/EF9/EF10 — logique métier du dépôt d'exercice.
 * RG6 : le relecteur est tiré au hasard parmi les présents à la session (hors auteur)
 * au moment du dépôt. EF10 (décision A2) : sans candidat, relecteur_id reste null,
 * l'exercice est créé EN_ATTENTE. Pas de vérification d'expiration ni de clôture (Q12/RG11).
 */
@Service
public class ExerciceService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;

    public ExerciceService(ExerciceRepository exerciceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository) {
        this.exerciceRepository = exerciceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
    }

    /**
     * EF7 : l'étudiant dépose le lien de son exercice.
     * Ordre des vérifications : session → étudiant → doublon → lien → assignation.
     */
    public ExerciceResponse deposerExercice(ExerciceRequest request) {
        // 1. La session (ressource cible) doit exister
        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(SessionNotFoundException::new);

        // 2. L'étudiant (auteur) doit exister — pas de vérification qu'il est présent (ticket)
        Etudiant auteur = etudiantRepository.findById(request.etudiantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        // 3. Un seul exercice par couple (session, étudiant)
        exerciceRepository.findBySessionIdAndEtudiantId(request.sessionId(), request.etudiantId())
                .ifPresent(exercice -> {
                    throw new ExerciceDejaDeposeException();
                });

        // 4. Le lien doit être une URL http/https exploitable
        validerLien(request.lien());

        // 5. EF9/RG6 : assignation d'un relecteur au moment du dépôt (peut être null, EF10)
        Etudiant relecteur = assignerRelecteur(request.sessionId(), request.etudiantId());

        // 6-7. Création — deposeAt rempli explicitement (Hibernate ignore les DEFAULT SQL)
        Exercice exercice = new Exercice(session, auteur, request.lien(),
                StatutExercice.EN_ATTENTE, LocalDateTime.now(), relecteur);
        Exercice enregistre = exerciceRepository.save(exercice);

        // 8.
        return new ExerciceResponse(enregistre.getId(), enregistre.getStatut().name());
    }

    /** Vérifie que le lien est une URI de schéma http ou https (sinon 400 LIEN_INVALIDE). */
    private void validerLien(String lien) {
        try {
            URI uri = new URI(lien);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new LienInvalideException();
            }
        } catch (URISyntaxException exception) {
            throw new LienInvalideException();
        }
    }

    /**
     * RG6 : tire au hasard un relecteur parmi les présents à la session, hors auteur.
     * EF10 (décision A2) : retourne null si aucun candidat disponible.
     */
    private Etudiant assignerRelecteur(Long sessionId, Long auteurId) {
        List<Presence> presences = presenceRepository.findBySessionId(sessionId);
        List<Long> candidats = presences.stream()
                .map(presence -> presence.getEtudiant().getId())
                .filter(id -> !id.equals(auteurId))
                .collect(Collectors.toList());

        if (candidats.isEmpty()) {
            return null;
        }

        Collections.shuffle(candidats);
        Long relecteurId = candidats.get(0);
        return etudiantRepository.findById(relecteurId).orElse(null);
    }

    /**
     * EF8 (décision A3) : l'étudiant remplace le lien de son exercice.
     * RG12 : refus si la relecture a déjà commencé (statut RELUE) → 409.
     * L'exercice garde son statut EN_ATTENTE et son relecteur (aucune réassignation).
     * Ordre des vérifications : exercice → statut → lien.
     */
    public ExerciceLienResponse remplacerLien(Long exerciceId, RemplacementLienRequest request) {
        // 1. L'exercice visé doit exister
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceNotFoundException::new);

        // 2. RG12 : la relecture ne doit pas avoir commencé
        if (exercice.getStatut() == StatutExercice.RELUE) {
            throw new RelectureDejaCommenceeException();
        }

        // 3. Le nouveau lien doit être une URI http/https exploitable (méthode de l'issue #6)
        validerLien(request.lien());

        // 4-5. Mise à jour du lien seul ; statut et relecteur inchangés
        exercice.setLien(request.lien());
        Exercice enregistre = exerciceRepository.save(exercice);

        // 6.
        return new ExerciceLienResponse(
                enregistre.getId(),
                enregistre.getStatut().name(),
                enregistre.getLien());
    }
}
