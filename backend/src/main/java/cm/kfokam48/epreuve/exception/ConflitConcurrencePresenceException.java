package cm.kfokam48.epreuve.exception;

/**
 * 409 DEJA_PRESENT levée quand deux requêtes concurrentes marquent la même
 * présence (bug #42) : la seconde INSERT est rejetée par la contrainte
 * uq_presence_session_etudiant (RG2). Le service traduit cette violation
 * en erreur métier pour renvoyer un 409 conforme au contrat au lieu d'une
 * 500 ERREUR_INATTENDUE (B4/ENF6).
 */
public class ConflitConcurrencePresenceException extends BusinessException {

    public ConflitConcurrencePresenceException() {
        super("DEJA_PRESENT",
                "L'étudiant a déjà marqué sa présence à cette session.", 409);
    }
}
