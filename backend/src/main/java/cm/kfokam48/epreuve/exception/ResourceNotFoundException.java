package cm.kfokam48.epreuve.exception;

/**
 * Erreur 404 — ressource introuvable (contrat B2 : format {code, message}).
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String code;

    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
