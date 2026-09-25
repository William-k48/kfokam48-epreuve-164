package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.EtudiantResponse;
import cm.kfokam48.epreuve.dto.PromotionResponse;
import cm.kfokam48.epreuve.service.PromotionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF19/EF21 — GET /api/promotions et GET /api/promotions/{id}/etudiants :
 * alimentent les listes déroulantes du frontend.
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<PromotionResponse> listerPromotions() {
        return promotionService.listerPromotions();
    }

    @GetMapping("/{id}/etudiants")
    public List<EtudiantResponse> listerEtudiants(@PathVariable Long id) {
        return promotionService.listerEtudiants(id);
    }
}
