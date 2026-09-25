package cm.kfokam48.epreuve.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * RG3 (décision A4) : après 5 erreurs consécutives, un étudiant est bloqué 2 minutes.
 * Comptage en mémoire, clé = etudiantId. Le compteur est remis à zéro
 * après un succès (via {@link #reinitialiser}) ou après expiration du blocage.
 */
@Service
public class CompteurTentativesService {

    private static final int SEUIL_ERREURS = 5;
    private static final int DUREE_BLOCAGE_MINUTES = 2;

    private final ConcurrentHashMap<Long, CompteurTentatives> compteurs = new ConcurrentHashMap<>();

    /** true si l'étudiant est actuellement bloqué. Un blocage expiré remet le compteur à zéro. */
    public boolean estBloque(Long etudiantId) {
        CompteurTentatives compteur = compteurs.get(etudiantId);
        if (compteur == null) {
            return false;
        }
        if (compteur.bloqueJusqua != null) {
            if (LocalDateTime.now().isBefore(compteur.bloqueJusqua)) {
                return true;
            }
            // RG3 : blocage expiré → remise à zéro
            compteurs.remove(etudiantId);
        }
        return false;
    }

    /** Incrémente le compteur d'erreurs ; à partir de 5 erreurs consécutives, bloque 2 minutes. */
    public void enregistrerErreur(Long etudiantId) {
        compteurs.compute(etudiantId, (id, compteur) -> {
            CompteurTentatives courant = (compteur == null) ? new CompteurTentatives() : compteur;
            courant.erreurs = courant.erreurs + 1;
            if (courant.erreurs >= SEUIL_ERREURS) {
                courant.bloqueJusqua = LocalDateTime.now().plusMinutes(DUREE_BLOCAGE_MINUTES);
            }
            return courant;
        });
    }

    /** Remet le compteur à zéro (appelé après une présence enregistrée). */
    public void reinitialiser(Long etudiantId) {
        compteurs.remove(etudiantId);
    }

    /** État interne : nombre d'erreurs consécutives et fin de blocage éventuelle. */
    private static class CompteurTentatives {

        private int erreurs;
        private LocalDateTime bloqueJusqua;
    }
}
