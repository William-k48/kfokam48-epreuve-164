package cm.kfokam48.epreuve.exception;

/**
 * 404 PROMOTION_INCONNUE — la promotion référencée par l'opération n'existe pas.
 * Sous-classe de ResourceNotFoundException : couverte par le handler existant
 * de GlobalExceptionHandler (404 + {code, message}).
 */
public class PromotionNotFoundException extends ResourceNotFoundException {

    public PromotionNotFoundException() {
        super("PROMOTION_INCONNUE", "La promotion demandée n'existe pas.");
    }
}
