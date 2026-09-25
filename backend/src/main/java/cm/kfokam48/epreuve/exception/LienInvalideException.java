package cm.kfokam48.epreuve.exception;

/**
 * 400 LIEN_INVALIDE — le lien déposé n'est pas une URL http/https exploitable.
 */
public class LienInvalideException extends BusinessException {

    public LienInvalideException() {
        super("LIEN_INVALIDE", "Le lien fourni n'est pas valide (http ou https attendu).", 400);
    }
}
