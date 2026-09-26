package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.ExerciceEtudiantResponse;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.exception.ResourceNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF15 — les exercices d'un étudiant, avec note et commentaire s'ils ont reçu
 * au moins une relecture.
 * v2 (RG5 v2, décision A9) : `note` est la MOYENNE des relectures rendues de
 * l'exercice ; `noteProvisoire` vaut true tant qu'un seul des deux relecteurs a
 * rendu (sa note est affichée en attendant), false quand les deux ont rendu,
 * null tant qu'aucune relecture n'est rendue.
 * RG7 : l'identité des relecteurs n'est jamais exposée dans la réponse.
 */
@Service
public class EtudiantService {

    private static final String CODE_ETUDIANT_INCONNU = "ETUDIANT_INCONNU";

    /** RG5 v2 : un exercice possède deux relecteurs. */
    private static final int NOMBRE_RELECTEURS = 2;

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
     * Liste les exercices de l'étudiant (note/commentaire null tant qu'aucune
     * relecture n'est rendue ; note provisoire si un seul rendu).
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

    /**
     * Map un exercice vers le DTO avec la note retenue (moyenne des relectures
     * rendues) et le flag provisoire. RG7 : aucun champ relecteur.
     */
    private ExerciceEtudiantResponse construireReponse(Exercice exercice) {
        List<Relecture> relecturesRendues = relectureRepository.findByExerciceId(exercice.getId());

        Integer note = null;
        Boolean noteProvisoire = null;
        String commentaire = null;

        if (!relecturesRendues.isEmpty()) {
            note = (int) Math.round(relecturesRendues.stream()
                    .mapToInt(Relecture::getNote)
                    .average()
                    .orElse(0.0));
            noteProvisoire = relecturesRendues.size() < NOMBRE_RELECTEURS;
            // Commentaire de la première relecture rendue (ordre chronologique)
            Relecture premiere = relecturesRendues.stream()
                    .min(Comparator.comparing(Relecture::getRendueAt))
                    .orElse(null);
            commentaire = premiere != null ? premiere.getCommentaire() : null;
        }

        return new ExerciceEtudiantResponse(
                exercice.getId(),
                exercice.getSession().getId(),
                exercice.getLien(),
                exercice.getStatut().name(),
                note,
                noteProvisoire,
                commentaire);
    }
}
