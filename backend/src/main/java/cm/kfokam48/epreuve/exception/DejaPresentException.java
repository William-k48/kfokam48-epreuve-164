package cm.kfokam48.epreuve.exception;

/**
 * 409 DEJA_PRESENT — l'étudiant a déjà marqué sa présence à cette session (RG2).
 */
public class DejaPresentException extends BusinessException {

    public DejaPresentException() {
        super("DEJA_PRESENT", "L'étudiant a déjà marqué sa présence à cette session.", 409);
    }
}
