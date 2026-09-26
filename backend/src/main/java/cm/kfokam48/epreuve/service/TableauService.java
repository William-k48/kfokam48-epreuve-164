package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.TableauLigneResponse;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.exception.PromotionNotFoundException;
import cm.kfokam48.epreuve.repository.AssignationRelectureRepository;
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
 * v2 (décision A9) : la moyenne est calculée sur les notes RETENUES de chaque
 * exercice (moyenne de ses relectures rendues, notes provisoires incluses).
 * Décision A7 : moyenne null si aucune note reçue. Promotion inconnue → 404 (EF17).
 */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final AssignationRelectureRepository assignationRelectureRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository,
                          EtudiantRepository etudiantRepository,
                          PresenceRepository presenceRepository,
                          ExerciceRepository exerciceRepository,
                          AssignationRelectureRepository assignationRelectureRepository,
                          RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.assignationRelectureRepository = assignationRelectureRepository;
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
        // v2 : relectures en attente = exercices où l'étudiant est assigné (≤ 2 par exercice)
        // et n'a pas encore rendu sa relecture — y compris les exercices EN_ATTENTE où
        // l'autre relecteur a déjà rendu (sa note reste à donner).
        long relecturesEnAttente =
                assignationRelectureRepository
                        .findDistinctExerciceByRelecteurIdAndExercice_Statut(etudiantId, StatutExercice.EN_ATTENTE)
                        .size();

        return new TableauLigneResponse(
                etudiantId,
                etudiant.getNom(),
                (int) presences,
                (int) exercicesDeposes,
                moyenne,
                (int) relecturesEnAttente);
    }

    /**
     * RG17/décision A7/A9 : moyenne des notes RETENUES de l'étudiant, exercice par
     * exercice (chaque exercice compte pour la moyenne de SES relectures rendues),
     * ou null si aucune note. Les notes provisoires (un seul relecteur rendu)
     * sont incluses, conformément au contrat v1.1.
     */
    private Double calculerMoyenne(Long etudiantId) {
        List<Exercice> exercices = exerciceRepository.findByEtudiantId(etudiantId);
        List<Double> notesRetenues = exercices.stream()
                .map(exercice -> relectureRepository.findByExerciceId(exercice.getId()))
                .filter(relectures -> !relectures.isEmpty())
                .map(relectures -> relectures.stream()
                        .mapToInt(Relecture::getNote)
                        .average()
                        .orElse(0.0))
                .collect(Collectors.toList());
        if (notesRetenues.isEmpty()) {
            return null;
        }
        return notesRetenues.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
    }
}
