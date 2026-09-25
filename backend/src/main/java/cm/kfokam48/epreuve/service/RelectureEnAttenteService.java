package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.RelectureEnAttenteResponse;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF18 (décision A6) — les relectures en attente d'un étudiant relecteur :
 * les exercices qui lui sont assignés et dont le statut est encore EN_ATTENTE.
 * L'auteur n'est jamais exposé.
 */
@Service
public class RelectureEnAttenteService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    private final EtudiantRepository etudiantRepository;
    private final ExerciceRepository exerciceRepository;

    public RelectureEnAttenteService(EtudiantRepository etudiantRepository,
                                     ExerciceRepository exerciceRepository) {
        this.etudiantRepository = etudiantRepository;
        this.exerciceRepository = exerciceRepository;
    }

    /**
     * Liste les exercices à relire pour l'étudiant {id} (statut EN_ATTENTE uniquement).
     */
    public List<RelectureEnAttenteResponse> listerRelecturesEnAttente(Long etudiantId) {
        // L'étudiant relecteur doit exister avant de lister
        etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        return exerciceRepository.findByRelecteurIdAndStatut(etudiantId, StatutExercice.EN_ATTENTE).stream()
                .map(this::construireReponse)
                .collect(Collectors.toList());
    }

    /** Map un exercice vers le DTO — aucun champ lié à l'auteur (anonymat). */
    private RelectureEnAttenteResponse construireReponse(Exercice exercice) {
        return new RelectureEnAttenteResponse(
                exercice.getId(),
                exercice.getLien(),
                exercice.getStatut().name());
    }
}
