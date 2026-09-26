package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.PresenceManuelleRequest;
import cm.kfokam48.epreuve.dto.PresenceRequest;
import cm.kfokam48.epreuve.dto.PresenceResponse;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Presence;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.entity.SourcePresence;
import cm.kfokam48.epreuve.exception.CodeExpireException;
import cm.kfokam48.epreuve.exception.CodeInconnuException;
import cm.kfokam48.epreuve.exception.ConflitConcurrencePresenceException;
import cm.kfokam48.epreuve.exception.DejaPresentException;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.exception.SessionNotFoundException;
import cm.kfokam48.epreuve.exception.TropDeTentativesException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF2/EF3/EF4/EF5 — logique métier du marquage de présence.
 * Ordre des vérifications imposé (Q4) : blocage → code → expiration → doublon.
 * Le doublon (RG2) n'incrémente PAS le compteur d'erreurs : ce n'est pas une erreur de code.
 *
 * Bug #42 : deux étudiants saisissant le code quasi simultanément passaient tous deux
 * le test de doublon (check-then-insert), et la seconde INSERT violait la contrainte
 * uq_presence_session_etudiant → 500. Correctif : marquerPresence est désormais
 * @Transactional et capture DataIntegrityViolationException pour renvoyer un 409
 * DEJA_PRESENT métier (via ConflitConcurrencePresenceException) au lieu d'une 500.
 */
@Service
public class PresenceService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    private final PresenceRepository presenceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final CompteurTentativesService compteurTentativesService;

    public PresenceService(PresenceRepository presenceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository,
                           CompteurTentativesService compteurTentativesService) {
        this.presenceRepository = presenceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.compteurTentativesService = compteurTentativesService;
    }

    /**
     * EF2 : l'étudiant marque sa présence avec le code (source toujours ETUDIANT ici).
     * @Transactional : la vérification de doublon et l'INSERT partagent la même
     * transaction ; la violation éventuelle de uq_presence_session_etudiant est
     * convertie en 409 DEJA_PRESENT (bug #42).
     */
    @Transactional
    public PresenceResponse marquerPresence(PresenceRequest request) {
        Long etudiantId = request.etudiantId();

        // 1. RG3 : le blocage est vérifié avant même de chercher le code (Q4)
        if (compteurTentativesService.estBloque(etudiantId)) {
            throw new TropDeTentativesException();
        }

        // 2. Code inconnu → incrément du compteur puis 400
        Session session = sessionRepository.findByCode(request.code())
                .orElseThrow(() -> {
                    compteurTentativesService.enregistrerErreur(etudiantId);
                    return new CodeInconnuException();
                });

        // 3. RG1 : code expiré → incrément du compteur puis 410
        if (LocalDateTime.now().isAfter(session.getExpirationAt())) {
            compteurTentativesService.enregistrerErreur(etudiantId);
            throw new CodeExpireException();
        }

        // 4. RG2 : déjà présent → 409 sans incrément (pas une erreur de code)
        presenceRepository.findBySessionIdAndEtudiantId(session.getId(), etudiantId)
                .ifPresent(presence -> {
                    throw new DejaPresentException();
                });

        // 5. L'étudiant doit exister
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        // 6-7. Création de la présence — ajouteeAt rempli explicitement (Hibernate ignore les DEFAULT SQL)
        // Bug #42 : si une requête concurrente insère la même présence entre-temps, la BDD
        // rejette l'INSERT (contrainte UNIQUE) → traduction en 409 métier, pas de 500.
        try {
            Presence presence = new Presence(session, etudiant, SourcePresence.ETUDIANT, LocalDateTime.now());
            Presence enregistree = presenceRepository.save(presence);
            presenceRepository.flush();

            // 8. Succès → remise à zéro du compteur
            compteurTentativesService.reinitialiser(etudiantId);

            return new PresenceResponse(
                    enregistree.getId(),
                    session.getId(),
                    etudiant.getId(),
                    enregistree.getSource().name());
        } catch (DataIntegrityViolationException exception) {
            // La contrainte uq_presence_session_etudiant a rejeté l'INSERT : la présence
            // concurrente a gagné la course. Réponse métier 409 DEJA_PRESENT (bug #42).
            throw new ConflitConcurrencePresenceException();
        }
    }

    /**
     * EF6 (décision A5) : le formateur ajoute manuellement une présence (Q14).
     * Pas de code requis, pas de vérification d'expiration (le formateur agit en admin),
     * pas de compteur RG3. Source forcée à FORMATEUR (RG13).
     * Ordre des vérifications : session → étudiant → doublon.
     */
    public PresenceResponse ajouterPresenceManuelle(Long sessionId, PresenceManuelleRequest request) {
        // 1. La session (ressource cible du chemin) doit exister
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        // 2. L'étudiant doit exister
        Etudiant etudiant = etudiantRepository.findById(request.etudiantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        // 3. RG2 : déjà présent → 409 (sans compteur : il n'y en a pas ici)
        presenceRepository.findBySessionIdAndEtudiantId(sessionId, request.etudiantId())
                .ifPresent(presence -> {
                    throw new DejaPresentException();
                });

        // 4-5. Création de la présence, source forcée à FORMATEUR (RG13), ajouteeAt explicite
        Presence presence = new Presence(session, etudiant, SourcePresence.FORMATEUR, LocalDateTime.now());
        Presence enregistree = presenceRepository.save(presence);

        // 6.
        return new PresenceResponse(
                enregistree.getId(),
                session.getId(),
                etudiant.getId(),
                enregistree.getSource().name());
    }
}
