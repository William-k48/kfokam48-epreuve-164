package cm.kfokam48.epreuve.exception;

/**
 * 409 RELECTURE_DEJA_RENDUE — la relecture est définitive, on ne la rend qu'une fois (RG9, décision A1).
 */
public class RelectureDejaRendueException extends BusinessException {

    public RelectureDejaRendueException() {
        super("RELECTURE_DEJA_RENDUE", "La relecture de cet exercice a déjà été rendue.", 409);
    }
}
