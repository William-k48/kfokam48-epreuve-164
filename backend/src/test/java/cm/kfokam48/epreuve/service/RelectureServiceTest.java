package cm.kfokam48.epreuve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.kfokam48.epreuve.dto.RelectureRequest;
import cm.kfokam48.epreuve.dto.RelectureResponse;
import cm.kfokam48.epreuve.entity.AssignationRelecture;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.RelectureDejaRendueException;
import cm.kfokam48.epreuve.repository.AssignationRelectureRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * B6 — tests unitaires de la règle RG5 v2 / décision A9 (deux relecteurs) :
 * la première relecture rendue laisse l'exercice EN_ATTENTE (note provisoire),
 * la seconde le fait passer RELUE (note définitive = moyenne des deux).
 * RG9 : une fois les deux rendues, toute nouvelle relecture → 409.
 */
@ExtendWith(MockitoExtension.class)
class RelectureServiceTest {

    private static final Long EXERCICE_ID = 10L;

    @Mock
    private RelectureRepository relectureRepository;

    @Mock
    private ExerciceRepository exerciceRepository;

    @Mock
    private AssignationRelectureRepository assignationRelectureRepository;

    @InjectMocks
    private RelectureService relectureService;

    private Exercice preparerExercice(StatutExercice statut) {
        Promotion promotion = new Promotion("L3");
        Session session = new Session("Cours", "ABC123",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), false, promotion);
        Etudiant auteur = new Etudiant("Aicha", promotion);
        Exercice exercice = new Exercice(session, auteur,
                "https://example.com/ex10.pdf", statut, LocalDateTime.now());
        when(exerciceRepository.findById(EXERCICE_ID)).thenReturn(Optional.of(exercice));
        return exercice;
    }

    private void preparerAssignations(Exercice exercice, Etudiant r1, Etudiant r2) {
        lenient().when(assignationRelectureRepository.findByExerciceId(EXERCICE_ID)).thenReturn(List.of(
                new AssignationRelecture(exercice, r1, LocalDateTime.now()),
                new AssignationRelecture(exercice, r2, LocalDateTime.now())));
    }

    @Test
    void premiereRelectureRendueGardeExerciceEnAttente() {
        Exercice exercice = preparerExercice(StatutExercice.EN_ATTENTE);
        Etudiant r1 = new Etudiant("Boris", exercice.getSession().getPromotion());
        Etudiant r2 = new Etudiant("Chloe", exercice.getSession().getPromotion());
        preparerAssignations(exercice, r1, r2);
        when(relectureRepository.findByExerciceId(EXERCICE_ID)).thenReturn(List.of());
        when(relectureRepository.save(any(Relecture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RelectureResponse response = relectureService.rendreRelecture(
                EXERCICE_ID, new RelectureRequest(14, "Bien."));

        assertEquals("EN_ATTENTE", response.statut());
        assertEquals(StatutExercice.EN_ATTENTE, exercice.getStatut());
        verify(exerciceRepository, never()).save(any());
    }

    @Test
    void deuxiemeRelectureRenduePasseExerciceRelue() {
        Exercice exercice = preparerExercice(StatutExercice.EN_ATTENTE);
        Etudiant r1 = new Etudiant("Boris", exercice.getSession().getPromotion());
        Etudiant r2 = new Etudiant("Chloe", exercice.getSession().getPromotion());
        preparerAssignations(exercice, r1, r2);

        Relecture relecturePrecedente = new Relecture(
                exercice, r1, 12, "Correct.", LocalDateTime.now());
        when(relectureRepository.findByExerciceId(EXERCICE_ID))
                .thenReturn(List.of(relecturePrecedente));
        when(relectureRepository.save(any(Relecture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RelectureResponse response = relectureService.rendreRelecture(
                EXERCICE_ID, new RelectureRequest(16, "Bien."));

        assertEquals("RELUE", response.statut());
        assertEquals(StatutExercice.RELUE, exercice.getStatut());
        verify(exerciceRepository).save(exercice);
    }

    @Test
    void resoumissionQuandDeuxRelecturesRenduesLeve409() {
        Exercice exercice = preparerExercice(StatutExercice.RELUE);
        Etudiant r1 = new Etudiant("Boris", exercice.getSession().getPromotion());
        Etudiant r2 = new Etudiant("Chloe", exercice.getSession().getPromotion());
        preparerAssignations(exercice, r1, r2);

        when(relectureRepository.findByExerciceId(EXERCICE_ID)).thenReturn(List.of(
                new Relecture(exercice, r1, 12, "Correct.", LocalDateTime.now()),
                new Relecture(exercice, r2, 16, "Bien.", LocalDateTime.now())));

        assertThrows(RelectureDejaRendueException.class,
                () -> relectureService.rendreRelecture(EXERCICE_ID, new RelectureRequest(10, "Tardif.")));
        verify(relectureRepository, never()).save(any());
    }
}
