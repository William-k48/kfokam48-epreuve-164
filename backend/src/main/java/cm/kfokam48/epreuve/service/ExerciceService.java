package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.ExerciceLienResponse;
import cm.kfokam48.epreuve.dto.ExerciceRelecteurResponse;
import cm.kfokam48.epreuve.dto.ExerciceRequest;
import cm.kfokam48.epreuve.dto.ExerciceResponse;
import cm.kfokam48.epreuve.dto.ReassignationRelecteurRequest;
import cm.kfokam48.epreuve.dto.RemplacementLienRequest;
import cm.kfokam48.epreuve.entity.AssignationRelecture;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Presence;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.ExerciceDejaDeposeException;
import cm.kfokam48.epreuve.exception.ExerciceNotFoundException;
import cm.kfokam48.epreuve.exception.LienInvalideException;
import cm.kfokam48.epreuve.exception.RelecteurInvalideException;
import cm.kfokam48.epreuve.exception.RelectureDejaCommenceeException;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.exception.SessionNotFoundException;
import cm.kfokam48.epreuve.repository.AssignationRelectureRepository;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF7/EF9/EF10 — logique métier du dépôt d'exercice.
 * v2 (RG6 v2, décision A9) : DEUX relecteurs distincts sont tirés au hasard
 * parmi les présents à la session (hors auteur) au moment du dépôt — un seul
 * si un seul candidat disponible (RG14 v2). Réassignation (EF11/RG15 v2) :
 * remplace un relecteur n'ayant pas encore rendu, ou complète la 2e assignation.
 */
@Service
public class ExerciceService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    /** RG5 v2 : chaque exercice est relu par au plus deux relecteurs. */
    private static final int NOMBRE_RELECTEURS = 2;

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final AssignationRelectureRepository assignationRelectureRepository;
    private final RelectureRepository relectureRepository;

    public ExerciceService(ExerciceRepository exerciceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository,
                           AssignationRelectureRepository assignationRelectureRepository,
                           RelectureRepository relectureRepository) {
        this.exerciceRepository = exerciceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.assignationRelectureRepository = assignationRelectureRepository;
        this.relectureRepository = relectureRepository;
    }

    /**
     * EF7 : l'étudiant dépose le lien de son exercice.
     * Ordre des vérifications : session → étudiant → doublon → lien → assignation.
     */
    @Transactional
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

        // 5. EF9/RG6 v2 : assignation de DEUX relecteurs au moment du dépôt (0 à 2, EF10/RG14)
        List<Etudiant> relecteurs = assignerRelecteurs(request.sessionId(), request.etudiantId());

        // 6-7. Création — deposeAt rempli explicitement (Hibernate ignore les DEFAULT SQL)
        Exercice exercice = new Exercice(session, auteur, request.lien(),
                StatutExercice.EN_ATTENTE, LocalDateTime.now());
        for (Etudiant relecteur : relecteurs) {
            exercice.ajouterAssignation(new AssignationRelecture(exercice, relecteur, LocalDateTime.now()));
        }
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
     * RG6 v2/A9 : tire au hasard DEUX relecteurs DISTINCTS parmi les présents à la
     * session, hors auteur. EF10/RG14 : retourne 0, 1 ou 2 candidats selon la
     * disponibilité (le dépôt n'est jamais bloqué pour autant).
     */
    private List<Etudiant> assignerRelecteurs(Long sessionId, Long auteurId) {
        List<Presence> presences = presenceRepository.findBySessionId(sessionId);
        List<Long> candidats = presences.stream()
                .map(presence -> presence.getEtudiant().getId())
                .filter(id -> !id.equals(auteurId))
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(candidats);
        return candidats.stream()
                .limit(NOMBRE_RELECTEURS)
                .map(id -> etudiantRepository.findById(id).orElse(null))
                .filter(relecteur -> relecteur != null)
                .collect(Collectors.toList());
    }

    /**
     * EF8 (décision A3) : l'étudiant remplace le lien de son exercice.
     * RG12 : refus dès que l'exercice est RELUE (les deux relectures rendues) → 409.
     * L'exercice garde son statut et ses relecteurs (aucune réassignation).
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

        // 4-5. Mise à jour du lien seul ; statut et relecteurs inchangés
        exercice.setLien(request.lien());
        Exercice enregistre = exerciceRepository.save(exercice);

        // 6.
        return new ExerciceLienResponse(
                enregistre.getId(),
                enregistre.getStatut().name(),
                enregistre.getLien());
    }

    /**
     * EF11 (décision A2, RG15 v2) : le formateur assigne/réassigne un relecteur
     * d'un exercice en attente. Deux cas :
     * - l'exercice a moins de 2 relecteurs → complément (ajout de la 2e assignation) ;
     * - l'exercice a déjà 2 relecteurs → remplacement du premier qui n'a pas rendu.
     * Ordre : exercice → statut → relecteur → auteur → limite RG5 v2.
     */
    @Transactional
    public ExerciceRelecteurResponse reassignerRelecteur(Long exerciceId, ReassignationRelecteurRequest request) {
        // 1. L'exercice visé doit exister
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceNotFoundException::new);

        // 2. RG12 : pas de réassignation après la fin de la relecture (RELUE)
        if (exercice.getStatut() == StatutExercice.RELUE) {
            throw new RelectureDejaCommenceeException();
        }

        // 3. Le nouveau relecteur doit exister
        Etudiant relecteur = etudiantRepository.findById(request.relecteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        // 4. Le relecteur ne peut pas être l'auteur
        if (relecteur.getId().equals(exercice.getEtudiant().getId())) {
            throw new RelecteurInvalideException();
        }

        // 5. Les assignations actuelles de l'exercice (0, 1 ou 2)
        List<AssignationRelecture> assignations = assignationRelectureRepository.findByExerciceId(exerciceId);
        boolean dejaAssigne = assignations.stream()
                .anyMatch(a -> a.getRelecteur().getId().equals(relecteur.getId()));

        // 6. RG5 v2 : si 2 assignations et le relecteur demandé n'en fait pas partie,
        //    on remplace le premier relecteur qui n'a PAS encore rendu sa relecture
        //    (RG15 v2 : un relecteur ayant rendu est figé, RG9).
        if (!dejaAssigne && assignations.size() >= NOMBRE_RELECTEURS) {
            AssignationRelecture aRemplacer = assignations.stream()
                    .filter(a -> relectureRepository
                            .findByExerciceIdAndRelecteurId(exerciceId, a.getRelecteur().getId())
                            .isEmpty())
                    .findFirst()
                    .orElseThrow(RelecteurInvalideException::new);
            assignationRelectureRepository.delete(aRemplacer);
        }

        // 7. Ajout de l'assignation (complément ou remplacement) si le relecteur
        //    n'est pas déjà assigné — l'UNIQUE(exercice_id, relecteur_id) protège aussi
        if (!dejaAssigne) {
            assignationRelectureRepository.save(
                    new AssignationRelecture(exercice, relecteur, LocalDateTime.now()));
        }

        // 8. Statut inchangé (EN_ATTENTE)
        return new ExerciceRelecteurResponse(
                exercice.getId(),
                exercice.getStatut().name(),
                relecteur.getId());
    }
}
