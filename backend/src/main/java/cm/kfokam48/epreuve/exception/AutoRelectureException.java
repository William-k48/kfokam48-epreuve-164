package cm.kfokam48.epreuve.exception;

/**
 * 403 AUTO_RELECTURE — un étudiant ne peut pas relire son propre exercice (RG4).
 */
public class AutoRelectureException extends BusinessException {

    public AutoRelectureException() {
        super("AUTO_RELECTURE", "Un étudiant ne peut pas relire son propre exercice.", 403);
    }
}
