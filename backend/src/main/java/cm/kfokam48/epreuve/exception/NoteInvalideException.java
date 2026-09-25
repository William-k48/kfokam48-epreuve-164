package cm.kfokam48.epreuve.exception;

/**
 * 400 NOTE_INVALIDE — la note n'est pas un entier entre 0 et 20 (RG8).
 */
public class NoteInvalideException extends BusinessException {

    public NoteInvalideException() {
        super("NOTE_INVALIDE", "La note doit être un entier entre 0 et 20.", 400);
    }
}
