package cm.kfokam48.epreuve.exception;

/**
 * 409 EXERCICE_DEJA_DEPOSE — un seul exercice par couple (session, étudiant) (V1__init.sql, UNIQUE).
 */
public class ExerciceDejaDeposeException extends BusinessException {

    public ExerciceDejaDeposeException() {
        super("EXERCICE_DEJA_DEPOSE", "Un exercice a déjà été déposé pour cette session par cet étudiant.", 409);
    }
}
