package cm.kfokam48.epreuve.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Gestion centralisée des erreurs (B4).
 * TOUTES les erreurs sortent au format { "code", "message" } — la page
 * d'erreur par défaut de Spring ne doit jamais apparaître (B2/B4).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 VALIDATION_ECHOUEE — payload invalide (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .findFirst()
                .orElse("Requête invalide");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ECHOUEE", message));
    }

    /** 404 RESSOURCE_INCONNUE — entité introuvable en base. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    /** Erreurs métier : le code décide du statut (409 DEJA_PRESENT, 410 CODE_EXPIRE, 429 QUOTA_DEPASSE, …). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    /** 404 — route inexistante (toujours au format imposé). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ROUTE_INCONNUE", "Ressource HTTP introuvable"));
    }

    /** 500 — filet de sécurité : jamais de stack trace vers le client (B4). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERREUR_INATTENDUE", "Une erreur interne est survenue"));
    }
}
