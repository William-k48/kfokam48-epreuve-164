package cm.kfokam48.epreuve.controller;

import cm.kfokam48.epreuve.dto.RelectureRequest;
import cm.kfokam48.epreuve.dto.RelectureResponse;
import cm.kfokam48.epreuve.exception.ErrorResponse;
import cm.kfokam48.epreuve.service.RelectureService;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EF12 — POST /api/relectures/{id} : le relecteur rend sa note et son commentaire.
 * Retourne 200 OK (pas 201). Aucun accès base ici : le contrôleur délègue au service (B3).
 *
 * RG8 : une note hors bornes (ex. 25) ou non entière (ex. 15.5) doit renvoyer
 * 400 NOTE_INVALIDE (contrat B2). Les deux handlers locaux ci-dessous réalisent ce
 * mapping SANS modifier GlobalExceptionHandler (interdit par le ticket) : un handler
 * local prime sur le @RestControllerAdvice, mais uniquement pour ce contrôleur.
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<RelectureResponse> rendreRelecture(
            @PathVariable Long id,
            @Valid @RequestBody RelectureRequest request) {
        RelectureResponse response = relectureService.rendreRelecture(id, request);
        return ResponseEntity.ok(response);
    }

    /** Erreur de validation portant sur la note → 400 NOTE_INVALIDE ; les autres → VALIDATION_ECHOUEE. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationLocale(MethodArgumentNotValidException ex) {
        boolean erreurSurNote = ex.getBindingResult().getFieldErrors().stream()
                .anyMatch(err -> "note".equals(err.getField()));
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .findFirst()
                .orElse("Requête invalide");
        String code = erreurSurNote ? "NOTE_INVALIDE" : "VALIDATION_ECHOUEE";
        return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
    }

    /** Note non entière (ex. 15.5) : rejetée par Jackson avant le service → 400 NOTE_INVALIDE. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCorpsIllisible(HttpMessageNotReadableException ex) {
        if (ex.getMostSpecificCause() instanceof InvalidFormatException
                && Integer.class.equals(((InvalidFormatException) ex.getMostSpecificCause()).getTargetType())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("NOTE_INVALIDE", "La note doit être un entier entre 0 et 20."));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ECHOUEE", "Corps de la requête illisible."));
    }
}
