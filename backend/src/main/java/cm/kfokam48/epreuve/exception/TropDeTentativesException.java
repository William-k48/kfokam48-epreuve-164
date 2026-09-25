package cm.kfokam48.epreuve.exception;

/**
 * 429 TROP_DE_TENTATIVES — 5 erreurs consécutives, l'étudiant est bloqué 2 minutes (RG3, décision A4).
 */
public class TropDeTentativesException extends BusinessException {

    public TropDeTentativesException() {
        super("TROP_DE_TENTATIVES", "Trop de tentatives, réessayez dans 2 minutes.", 429);
    }
}
