package cm.kfokam48.epreuve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.kfokam48.epreuve.dto.PresenceRequest;
import cm.kfokam48.epreuve.dto.PresenceResponse;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Presence;
import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.exception.CodeExpireException;
import cm.kfokam48.epreuve.exception.CodeInconnuException;
import cm.kfokam48.epreuve.exception.ConflitConcurrencePresenceException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * B6 — tests unitaires des règles métier de PresenceService, sans base de données.
 * RG1 : un code dont expirationAt est dépassé ne permet plus de marquer sa présence.
 */
@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    private static final Long ETUDIANT_ID = 1L;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EtudiantRepository etudiantRepository;

    @Mock
    private CompteurTentativesService compteurTentativesService;

    @InjectMocks
    private PresenceService presenceService;

    @Test
    void codeExpireLeveCodeExpireException() {
        Session sessionExpiree = new Session("Cours", "ABC123",
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().minusMinutes(15),
                false, new Promotion("L3"));
        when(sessionRepository.findByCode("ABC123")).thenReturn(Optional.of(sessionExpiree));

        assertThrows(CodeExpireException.class,
                () -> presenceService.marquerPresence(new PresenceRequest("ABC123", ETUDIANT_ID)));
    }

    @Test
    void codeInconnuLeveCodeInconnuException() {
        when(sessionRepository.findByCode("ZZZZZZ")).thenReturn(Optional.empty());

        assertThrows(CodeInconnuException.class,
                () -> presenceService.marquerPresence(new PresenceRequest("ZZZZZZ", ETUDIANT_ID)));
    }

    @Test
    void codeValideEnregistreLaPresenceAvecSourceEtudiant() {
        Promotion promotion = new Promotion("L3");
        Session sessionValide = new Session("Cours", "ABC123",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), false, promotion);
        Etudiant etudiant = new Etudiant("Aicha", promotion);

        when(sessionRepository.findByCode("ABC123")).thenReturn(Optional.of(sessionValide));
        when(presenceRepository.findBySessionIdAndEtudiantId(sessionValide.getId(), ETUDIANT_ID))
                .thenReturn(Optional.empty());
        when(etudiantRepository.findById(ETUDIANT_ID)).thenReturn(Optional.of(etudiant));
        when(presenceRepository.save(any(Presence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PresenceResponse response = presenceService.marquerPresence(new PresenceRequest("ABC123", ETUDIANT_ID));

        assertEquals("ETUDIANT", response.source());
        verify(compteurTentativesService).reinitialiser(ETUDIANT_ID);
    }

    /**
     * Bug #42 : deux requêtes concurrentes avec le même code passent toutes deux
     * le test de doublon ; la seconde INSERT est rejetée par la contrainte
     * uq_presence_session_etudiant. Le service doit traduire cette violation
     * en 409 DEJA_PRESENT (ConflitConcurrencePresenceException) et non laisser
     * remonter une 500 ERREUR_INATTENDUE.
     */
    @Test
    void violationContrainteUniqueConcurrenteLevee409DejaPresent() {
        Promotion promotion = new Promotion("L3");
        Session sessionValide = new Session("Cours", "ABC123",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), false, promotion);
        Etudiant etudiant = new Etudiant("Aicha", promotion);

        when(sessionRepository.findByCode("ABC123")).thenReturn(Optional.of(sessionValide));
        when(presenceRepository.findBySessionIdAndEtudiantId(sessionValide.getId(), ETUDIANT_ID))
                .thenReturn(Optional.empty());
        when(etudiantRepository.findById(ETUDIANT_ID)).thenReturn(Optional.of(etudiant));
        when(presenceRepository.save(any(Presence.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "uq_presence_session_etudiant"));

        assertThrows(ConflitConcurrencePresenceException.class,
                () -> presenceService.marquerPresence(new PresenceRequest("ABC123", ETUDIANT_ID)));
        verify(compteurTentativesService, never()).reinitialiser(ETUDIANT_ID);
    }
}
