package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.ExerciceEtudiantResponse;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF15 — les exercices d'un étudiant, avec note et commentaire s'ils sont relus.
 * RG7 : l'identité du relecteur n'est jamais exposée dans la réponse.
 */
@Service
public class EtudiantService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    private final EtudiantRepository etudiantRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public EtudiantService(EtudiantRepository etudiantRepository,
                           ExerciceRepository exerciceRepository,
                           RelectureRepository relectureRepository) {
        this.etudiantRepository = etudiantRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    /**
     * Liste les exercices de l'étudiant (note et commentaire null si pas encore relu).
     */
    public List<ExerciceEtudiantResponse> listerExercices(Long etudiantId) {
        // L'étudiant doit exister avant de lister
        etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        return exerciceRepository.findByEtudiantId(etudiantId).stream()
                .map(this::construireReponse)
                .collect(Collectors.toList());
    }

    /** Map un exercice vers le DTO, en récupérant note/commentaire si une relecture existe. */
    private ExerciceEtudiantResponse construireReponse(Exercice exercice) {
        Optional<Relecture> relecture = relectureRepository.findByExerciceId(exercice.getId());

        Integer note = null;
        String commentaire = null;
        if (relecture.isPresent()) {
            note = relecture.get().getNote();
            commentaire = relecture.get().getCommentaire();
        }

        return new ExerciceEtudiantResponse(
                exercice.getId(),
                exercice.getSession().getId(),
                exercice.getLien(),
                exercice.getStatut().name(),
                note,
                commentaire);
    }
}
