package cm.kfokam48.epreuve.exception;

/**
 * 400 CODE_INCONNU — le code saisi ne correspond à aucune session ouverte.
 */
public class CodeInconnuException extends BusinessException {

    public CodeInconnuException() {
        super("CODE_INCONNU", "Le code saisi ne correspond à aucune session.", 400);
    }
}
