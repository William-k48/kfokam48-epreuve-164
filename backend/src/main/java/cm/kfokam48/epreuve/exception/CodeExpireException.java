package cm.kfokam48.epreuve.exception;

/**
 * 410 CODE_EXPIRE — le code de présence a expiré (RG1 : 15 minutes après l'ouverture).
 */
public class CodeExpireException extends BusinessException {

    public CodeExpireException() {
        super("CODE_EXPIRE", "Le code de présence a expiré.", 410);
    }
}
