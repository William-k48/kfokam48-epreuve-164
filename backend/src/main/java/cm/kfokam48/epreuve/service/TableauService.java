package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.TableauLigneResponse;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.PromotionNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.PromotionRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF16/EF17 — tableau récapitulatif du formateur (RG16).
 * RG17 : la moyenne est calculée ici (API), jamais par le frontend.
 * Décision A7 : moyenne null si aucune note reçue. Promotion inconnue → 404 (EF17).
 */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository,
                          EtudiantRepository etudiantRepository,
                          PresenceRepository presenceRepository,
                          ExerciceRepository exerciceRepository,
                          RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    /**
     * Construit le tableau de la promotion : 4 indicateurs par étudiant, trié par nom.
     */
    public List<TableauLigneResponse> construireTableau(Long promotionId) {
        // La promotion doit exister avant de construire la liste (EF17)
        promotionRepository.findById(promotionId)
                .orElseThrow(PromotionNotFoundException::new);

        return etudiantRepository.findByPromotionId(promotionId).stream()
                .sorted(Comparator.comparing(Etudiant::getNom))
                .map(this::construireLigne)
                .collect(Collectors.toList());
    }

    /** Agrège les 4 indicateurs d'un étudiant. */
    private TableauLigneResponse construireLigne(Etudiant etudiant) {
        Long etudiantId = etudiant.getId();

        long presences = presenceRepository.countByEtudiantId(etudiantId);
        long exercicesDeposes = exerciceRepository.countByEtudiantId(etudiantId);
        Double moyenne = calculerMoyenne(etudiantId);
        long relecturesEnAttente =
                exerciceRepository.countByRelecteurIdAndStatut(etudiantId, StatutExercice.EN_ATTENTE);

        return new TableauLigneResponse(
                etudiantId,
                etudiant.getNom(),
                (int) presences,
                (int) exercicesDeposes,
                moyenne,
                (int) relecturesEnAttente);
    }

    /** RG17/décision A7 : moyenne des notes reçues, ou null si aucune note. */
    private Double calculerMoyenne(Long etudiantId) {
        List<Relecture> relecturesRecues = relectureRepository.findByExerciceEtudiantId(etudiantId);
        if (relecturesRecues.isEmpty()) {
            return null;
        }
        return relecturesRecues.stream()
                .mapToInt(Relecture::getNote)
                .average()
                .orElse(Double.NaN);
    }
}
