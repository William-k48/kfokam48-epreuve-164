package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.RelectureEnAttenteResponse;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.repository.AssignationRelectureRepository;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF18 (décision A6) — les relectures en attente d'un étudiant relecteur.
 * v2 (RG5 v2, décision A9) : un exercice possède deux relecteurs ; un exercice
 * est listé pour CE relecteur tant qu'il est assigné et qu'il n'a PAS encore
 * rendu sa relecture — même si l'autre relecteur a déjà rendu (note provisoire).
 * L'auteur n'est jamais exposé.
 */
@Service
public class RelectureEnAttenteService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    private final EtudiantRepository etudiantRepository;
    private final AssignationRelectureRepository assignationRelectureRepository;

    public RelectureEnAttenteService(EtudiantRepository etudiantRepository,
                                     AssignationRelectureRepository assignationRelectureRepository) {
        this.etudiantRepository = etudiantRepository;
        this.assignationRelectureRepository = assignationRelectureRepository;
    }

    /**
     * Liste les exercices à relire pour l'étudiant {id} : il est assigné dessus
     * et n'a pas encore rendu sa relecture (décision A10 : c'est lui que
     * l'attente désigne).
     */
    public List<RelectureEnAttenteResponse> listerRelecturesEnAttente(Long etudiantId) {
        // L'étudiant relecteur doit exister avant de lister
        etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CODE_ETUDIANT_INCONNU, "L'étudiant demandé n'existe pas."));

        return assignationRelectureRepository
                .findDistinctExerciceByRelecteurIdAndExercice_Statut(etudiantId, StatutExercice.EN_ATTENTE)
                .stream()
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
