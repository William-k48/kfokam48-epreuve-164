package cm.kfokam48.epreuve.exception;

/**
 * 409 RELECTURE_DEJA_COMMENCEE — l'exercice est déjà relu (RG12, décision A1).
 */
public class RelectureDejaCommenceeException extends BusinessException {

    public RelectureDejaCommenceeException() {
        super("RELECTURE_DEJA_COMMENCEE", "La relecture de cet exercice a déjà commencé.", 409);
    }
}
