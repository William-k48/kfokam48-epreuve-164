package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.TableauLigneResponse;
import cm.kfokam48.epreuve.exception.ErrorResponse;
import cm.kfokam48.epreuve.service.TableauService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF16 — GET /api/tableau : tableau récapitulatif du formateur pour une promotion.
 * Aucun accès base ici : le contrôleur délègue au service (B3).
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping
    public List<TableauLigneResponse> construireTableau(@RequestParam Long promotionId) {
        return tableauService.construireTableau(promotionId);
    }

    /** promotionId absent de la requête → 400 VALIDATION_ECHOUEE (format B4, sans toucher au handler global). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametreManquant(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ECHOUEE",
                        "Le paramètre " + ex.getParameterName() + " est obligatoire"));
    }
}
