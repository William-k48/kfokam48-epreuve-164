package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.ExerciceEtudiantResponse;
import cm.kfokam48.epreuve.dto.RelectureEnAttenteResponse;
import cm.kfokam48.epreuve.service.EtudiantService;
import cm.kfokam48.epreuve.service.RelectureEnAttenteService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF15 — GET /api/etudiants/{id}/exercices : les exercices de l'étudiant avec
 * leur note et leur commentaire s'ils sont relus (RG7 : relecteur jamais exposé).
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final RelectureEnAttenteService relectureEnAttenteService;

    public EtudiantController(EtudiantService etudiantService,
                              RelectureEnAttenteService relectureEnAttenteService) {
        this.etudiantService = etudiantService;
        this.relectureEnAttenteService = relectureEnAttenteService;
    }

    @GetMapping("/{id}/exercices")
    public List<ExerciceEtudiantResponse> listerExercices(@PathVariable Long id) {
        return etudiantService.listerExercices(id);
    }

    /**
     * EF18 (décision A6) — GET /api/etudiants/{id}/relectures : les exercices à relire
     * pour ce relecteur (EN_ATTENTE uniquement). L'auteur n'apparaît jamais.
     */
    @GetMapping("/{id}/relectures")
    public List<RelectureEnAttenteResponse> listerRelecturesEnAttente(@PathVariable Long id) {
        return relectureEnAttenteService.listerRelecturesEnAttente(id);
    }
}
