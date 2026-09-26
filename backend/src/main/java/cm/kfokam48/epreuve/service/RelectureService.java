package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.RelectureRequest;
import cm.kfokam48.epreuve.dto.RelectureResponse;
import cm.kfokam48.epreuve.entity.AssignationRelecture;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.AutoRelectureException;
import cm.kfokam48.epreuve.exception.ExerciceNotFoundException;
import cm.kfokam48.epreuve.exception.NoteInvalideException;
import cm.kfokam48.epreuve.exception.RelecteurInvalideException;
import cm.kfokam48.epreuve.exception.RelectureDejaRendueException;
import cm.kfokam48.epreuve.repository.AssignationRelectureRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF12/EF13/EF14 — logique métier du rendu de relecture.
 * v2 (RG5 v2, décision A9) : un exercice possède DEUX relecteurs. La PREMIÈRE
 * relecture rendue laisse l'exercice EN_ATTENTE (note provisoire = moyenne des
 * relectures rendues) ; la SECONDE le fait passer RELUE et fixe la note
 * définitive (moyenne des deux). Chaque relecture individuelle reste définitive
 * (RG9/A1) : toute resoumission quand les deux sont rendues → 409.
 * Décision A10 (sans auth) : l'appel émane du relecteur assigné qui n'a pas
 * encore rendu sa relecture.
 */
@Service
public class RelectureService {

    /** RG5 v2 : un exercice possède exactement deux relecteurs quand ils existent. */
    private static final int NOMBRE_RELECTEURS = 2;

    private final RelectureRepository relectureRepository;
    private final ExerciceRepository exerciceRepository;
    private final AssignationRelectureRepository assignationRelectureRepository;

    public RelectureService(RelectureRepository relectureRepository,
                            ExerciceRepository exerciceRepository,
                            AssignationRelectureRepository assignationRelectureRepository) {
        this.relectureRepository = relectureRepository;
        this.exerciceRepository = exerciceRepository;
        this.assignationRelectureRepository = assignationRelectureRepository;
    }

    /**
     * EF12 : un relecteur rend sa note et son commentaire. Retourne 200, pas 201.
     * Ordre des vérifications : exercice → relectures complètes (RG9) → note →
     * relecteur (A10) → auto-relecture (RG4).
     */
    @Transactional
    public RelectureResponse rendreRelecture(Long exerciceId, RelectureRequest request) {
        // 1. L'exercice visé doit exister
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceNotFoundException::new);

        // 2. RG9 : les DEUX relectures sont déjà rendues → exercice RELUE, définitif
        List<Relecture> relecturesRendues = relectureRepository.findByExerciceId(exerciceId);
        if (relecturesRendues.size() >= NOMBRE_RELECTEURS
                || exercice.getStatut() == StatutExercice.RELUE) {
            throw new RelectureDejaRendueException();
        }

        // 3. RG8 : note entière entre 0 et 20 (double filet avec @Min/@Max du DTO)
        if (request.note() == null || request.note() < 0 || request.note() > 20) {
            throw new NoteInvalideException();
        }

        // 4. A10 : le relecteur est l'assigné qui n'a pas encore rendu sa relecture.
        // Les entités non persistées peuvent avoir un id null : la comparaison
        // d'identité se fait d'abord par référence, puis par id.
        List<AssignationRelecture> assignations = assignationRelectureRepository.findByExerciceId(exerciceId);
        AssignationRelecture assignationCourante = assignations.stream()
                .filter(a -> relecturesRendues.stream().noneMatch(r ->
                        memeEtudiant(r.getRelecteur(), a.getRelecteur())))
                .findFirst()
                .orElseThrow(RelecteurInvalideException::new);
        Etudiant relecteurCourant = assignationCourante.getRelecteur();

        // 5. RG4 : le relecteur ne peut pas être l'auteur (garde-fou, en plus de l'assignation)
        if (memeEtudiant(relecteurCourant, exercice.getEtudiant())) {
            throw new AutoRelectureException();
        }

        // 6. Relecture enregistrée pour CE relecteur (UNE par couple, RG9 v2)
        Relecture relecture = new Relecture(exercice, relecteurCourant, request.note(),
                request.commentaire(), LocalDateTime.now());
        relectureRepository.save(relecture);

        // 7. v2 : statut de l'exercice selon le nombre de relectures rendues
        boolean toutesRendues = relecturesRendues.size() + 1 >= NOMBRE_RELECTEURS;
        if (toutesRendues) {
            exercice.setStatut(StatutExercice.RELUE);
            exerciceRepository.save(exercice);
        }

        // 8. La note retournée est la note de la relecture qui vient d'être rendue
        //    (le contrat reste { id, exerciceId, note, statut } — inchangé, B2)
        return new RelectureResponse(
                relecture.getId(),
                exercice.getId(),
                relecture.getNote(),
                exercice.getStatut().name());
    }

    /** Identité d'étudiant robuste : référence, puis id (non null en BDD). */
    private static boolean memeEtudiant(Etudiant a, Etudiant b) {
        if (a == b) {
            return true;
        }
        return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
    }
}
