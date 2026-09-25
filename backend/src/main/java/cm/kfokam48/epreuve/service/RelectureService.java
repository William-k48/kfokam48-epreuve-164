package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.RelectureRequest;
import cm.kfokam48.epreuve.dto.RelectureResponse;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.AutoRelectureException;
import cm.kfokam48.epreuve.exception.ExerciceNotFoundException;
import cm.kfokam48.epreuve.exception.NoteInvalideException;
import cm.kfokam48.epreuve.exception.RelectureDejaRendueException;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF12/EF13/EF14 — logique métier du rendu de relecture.
 * RG4 : pas d'auto-relecture (403). RG8 : note entière 0-20 (400).
 * RG9 (décision A1) : la relecture est définitive (409 si déjà rendue).
 * Pas d'auth : le relecteur est exercice.relecteur. L'exercice passe à RELUE
 * dans la même transaction que l'enregistrement de la relecture.
 */
@Service
public class RelectureService {

    private final RelectureRepository relectureRepository;
    private final ExerciceRepository exerciceRepository;

    public RelectureService(RelectureRepository relectureRepository, ExerciceRepository exerciceRepository) {
        this.relectureRepository = relectureRepository;
        this.exerciceRepository = exerciceRepository;
    }

    /**
     * EF12 : le relecteur rend sa note et son commentaire. Retourne 200, pas 201.
     * Ordre des vérifications : exercice → déjà rendue → note → auto-relecture.
     */
    @Transactional
    public RelectureResponse rendreRelecture(Long exerciceId, RelectureRequest request) {
        // 1. L'exercice visé doit exister
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceNotFoundException::new);

        // 2. RG9 : la relecture est définitive
        if (exercice.getStatut() == StatutExercice.RELUE) {
            throw new RelectureDejaRendueException();
        }

        // 3. RG8 : note entière entre 0 et 20 (double filet avec @Min/@Max du DTO)
        if (request.note() == null || request.note() < 0 || request.note() > 20) {
            throw new NoteInvalideException();
        }

        // 4. RG4 : le relecteur (exercice.relecteur, pas d'auth) ne peut pas être l'auteur
        if (exercice.getRelecteur() != null
                && exercice.getRelecteur().getId().equals(exercice.getEtudiant().getId())) {
            throw new AutoRelectureException();
        }

        // 5-7. Relecture enregistrée et exercice passé à RELUE dans la même transaction
        Relecture relecture = new Relecture(exercice, exercice.getRelecteur(), request.note(),
                request.commentaire(), LocalDateTime.now());
        Relecture enregistree = relectureRepository.save(relecture);

        exercice.setStatut(StatutExercice.RELUE);
        Exercice exerciceRelu = exerciceRepository.save(exercice);

        // 8.
        return new RelectureResponse(
                enregistree.getId(),
                exerciceRelu.getId(),
                enregistree.getNote(),
                exerciceRelu.getStatut().name());
    }
}
