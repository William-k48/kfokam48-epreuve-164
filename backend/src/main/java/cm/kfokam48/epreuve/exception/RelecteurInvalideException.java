package cm.kfokam48.epreuve.exception;

/**
 * 400 RELECTEUR_INVALIDE — le relecteur ne peut pas être l'auteur de l'exercice (EF11).
 */
public class RelecteurInvalideException extends BusinessException {

    public RelecteurInvalideException() {
        super("RELECTEUR_INVALIDE", "Le relecteur ne peut pas être l'auteur de l'exercice.", 400);
    }
}
