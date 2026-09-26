package cm.kfokam48.epreuve.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Issue #35 — CORS entre le frontend et l'API.
 *
 * Le frontend est servi sur le port 4200 (Vite en dev, Nginx en production)
 * et l'API répond sur le port 8085 : ce sont deux origines différentes, donc
 * le navigateur bloque les appels sans en-têtes CORS sur la réponse.
 *
 * Deux origines autorisées :
 * - http://localhost:4200 : navigateur du poste du développeur, ou navigateur
 *   de la machine hôte face au conteneur Nginx exposé sur le port 4200 ;
 * - http://frontend:4200 : appel depuis un autre conteneur du réseau Docker.
 *
 * Les proxys (Vite en dev, Nginx en production) sont une seconde voie :
 * le CORS couvre le cas où le navigateur joint directement le port 8085.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://frontend:4200")
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .maxAge(3600);
    }
}
