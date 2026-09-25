package cm.kfokam48.epreuve.service;

import cm.kfokam48.epreuve.dto.EtudiantResponse;
import cm.kfokam48.epreuve.dto.PromotionResponse;
import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.exception.PromotionNotFoundException;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.PromotionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * EF19/EF21 — listes déroulantes : promotions disponibles et étudiants d'une promotion.
 */
@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    public PromotionService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    /** Liste toutes les promotions (EF21). */
    public List<PromotionResponse> listerPromotions() {
        return promotionRepository.findAll().stream()
                .sorted(Comparator.comparing(Promotion::getNom))
                .map(promotion -> new PromotionResponse(promotion.getId(), promotion.getNom()))
                .collect(Collectors.toList());
    }

    /** Liste les étudiants d'une promotion (EF19) — 404 PROMOTION_INCONNUE si absente. */
    public List<EtudiantResponse> listerEtudiants(Long promotionId) {
        // La promotion doit exister avant de lister
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(PromotionNotFoundException::new);

        return etudiantRepository.findByPromotionId(promotion.getId()).stream()
                .sorted(Comparator.comparing(Etudiant::getNom))
                .map(etudiant -> new EtudiantResponse(etudiant.getId(), etudiant.getNom()))
                .collect(Collectors.toList());
    }
}
