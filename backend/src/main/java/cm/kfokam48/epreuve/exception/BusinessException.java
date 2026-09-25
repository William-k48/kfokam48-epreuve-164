package cm.kfokam48.epreuve.exception;

/**
 * Violation d'une règle métier. Le porteur du code décide du statut HTTP
 * (ex. CODE_EXPIRE → 410, DEJA_PRESENT → 409, AUTO_RELECTURE → 400).
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final int status;

    public BusinessException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
