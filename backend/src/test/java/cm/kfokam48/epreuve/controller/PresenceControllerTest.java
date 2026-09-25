package cm.kfokam48.epreuve.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cm.kfokam48.epreuve.entity.Promotion;
import cm.kfokam48.epreuve.entity.Session;
import cm.kfokam48.epreuve.repository.PromotionRepository;
import cm.kfokam48.epreuve.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * B6 — test d'intégration de POST /api/presences sur H2 (profil test, sans PostgreSQL).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceControllerTest {

    private static final String CODE_TEST = "TEST01";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Long sessionId;

    @BeforeEach
    void preparerDonnees() {
        Promotion promotion = promotionRepository.save(new Promotion("Promotion test"));
        LocalDateTime ouvertureAt = LocalDateTime.now();
        Session session = sessionRepository.save(new Session(
                "Session de test", CODE_TEST, ouvertureAt, ouvertureAt.plusMinutes(15), false, promotion));
        sessionId = session.getId();
    }

    @Test
    void presenceValideRenvoie201AvecSourceEtudiant() throws Exception {
        String body = "{\"code\":\"" + CODE_TEST + "\",\"etudiantId\":1}";

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    void codeInconnuRenvoie400AvecCodeErreur() throws Exception {
        String body = "{\"code\":\"ZZZZZZ\",\"etudiantId\":1}";

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
