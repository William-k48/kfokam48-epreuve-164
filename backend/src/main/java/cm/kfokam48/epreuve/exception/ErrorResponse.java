package cm.kfokam48.epreuve.exception;

/**
 * Corps d'erreur imposé par le sujet (B4) et par le contrat api/contrat.yaml (B2) :
 * { "code": "...", "message": "..." } — jamais de stack trace.
 */
public record ErrorResponse(String code, String message) {
}
