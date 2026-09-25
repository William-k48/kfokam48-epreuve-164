package cm.kfokam48.epreuve.config;

import cm.kfokam48.epreuve.entity.Etudiant;
import cm.kfokam48.epreuve.entity.Exercice;
import cm.kfokam48.epreuve.entity.Presence;
import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.entity.Relecture;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.entity.SourcePresence;
import cm.kfokam48.epreuve.entity.StatutExercice;
import cm.kfokam48.epreuve.repository.EtudiantRepository;
import cm.kfokam48.epreuve.repository.ExerciceRepository;
import cm.kfokam48.epreuve.repository.PresenceRepository;
import cm.kfokam48.epreuve.repository.PromotionRepository;
import cm.kfokam48.epreuve.repository.RelectureRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * EF20/ENF10 — données de démonstration chargées au démarrage.
 * Ne s'exécute QUE si la base est vide (aucune promotion existante) :
 * 1 promotion, 10 étudiants, 1 session ouverte (code valable 15 minutes, RG1),
 * des présences, 3 exercices dont 2 relus — pour que le tableau ne soit pas vide.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final SessionRepository sessionRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public DataInitializer(PromotionRepository promotionRepository,
                           EtudiantRepository etudiantRepository,
                           SessionRepository sessionRepository,
                           PresenceRepository presenceRepository,
                           ExerciceRepository exerciceRepository,
                           RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.sessionRepository = sessionRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    @Override
    public void run(String... args) {
        // Garde-fou : ne rien créer si des données existent déjà
        if (promotionRepository.count() > 0) {
            return;
        }

        Promotion promotion = promotionRepository.save(new Promotion("L3 Informatique 2025"));

        List<String> noms = List.of("Aicha", "Boris", "Chloe", "David", "Emma",
                "Fabrice", "Gaelle", "Herve", "Ines", "Jonas");
        List<Etudiant> etudiants = noms.stream()
                .map(nom -> etudiantRepository.save(new Etudiant(nom, promotion)))
                .collect(Collectors.toList());

        // Session ouverte : code 6 caractères, expiration à +15 minutes (RG1)
        LocalDateTime ouvertureAt = LocalDateTime.now();
        Session session = sessionRepository.save(new Session(
                "Seance de demonstration", "DEMO01",
                ouvertureAt, ouvertureAt.plusMinutes(15), false, promotion));

        // Les 6 premiers étudiants marquent leur présence (source ETUDIANT)
        for (Etudiant present : etudiants.subList(0, 6)) {
            presenceRepository.save(new Presence(
                    session, present, SourcePresence.ETUDIANT, LocalDateTime.now()));
        }

        // 3 exercices : 2 relus (tableau non vide), 1 en attente de relecture
        Etudiant auteur1 = etudiants.get(0);
        Etudiant auteur2 = etudiants.get(1);
        Etudiant auteur3 = etudiants.get(2);
        Etudiant relecteur1 = etudiants.get(3);
        Etudiant relecteur2 = etudiants.get(4);
        Etudiant relecteur3 = etudiants.get(5);

        Exercice exercice1 = exerciceRepository.save(new Exercice(session, auteur1,
                "https://example.com/demo/exercice-1.pdf", StatutExercice.RELUE,
                LocalDateTime.now(), relecteur1));
        Exercice exercice2 = exerciceRepository.save(new Exercice(session, auteur2,
                "https://example.com/demo/exercice-2.pdf", StatutExercice.RELUE,
                LocalDateTime.now(), relecteur2));
        exerciceRepository.save(new Exercice(session, auteur3,
                "https://example.com/demo/exercice-3.pdf", StatutExercice.EN_ATTENTE,
                LocalDateTime.now(), relecteur3));

        // RG9 : une relecture définitive par exercice relu
        relectureRepository.save(new Relecture(exercice1, relecteur1, 14,
                "Bon travail, etoffe la partie 2.", LocalDateTime.now()));
        relectureRepository.save(new Relecture(exercice2, relecteur2, 17,
                "Tres complet et bien structure.", LocalDateTime.now()));
    }
}
