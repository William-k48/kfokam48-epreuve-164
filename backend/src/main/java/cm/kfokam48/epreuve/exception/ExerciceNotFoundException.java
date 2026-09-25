package cm.kfokam48.epreuve.exception;

/**
 * 404 EXERCICE_INCONNU — l'exercice visé n'existe pas.
 */
public class ExerciceNotFoundException extends ResourceNotFoundException {

    public ExerciceNotFoundException() {
        super("EXERCICE_INCONNU", "L'exercice demandé n'existe pas.");
    }
}
