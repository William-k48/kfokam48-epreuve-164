package cm.kfokam48.epreuve.exception;

/**
 * 404 SESSION_INCONNUE — la session visée n'existe pas.
 */
public class SessionNotFoundException extends ResourceNotFoundException {

    public SessionNotFoundException() {
        super("SESSION_INCONNUE", "La session demandée n'existe pas.");
    }
}
