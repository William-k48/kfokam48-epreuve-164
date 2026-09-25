package cm.kfokam48.epreuve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Présence d'un étudiant à une session (RG2 : une seule par couple session/étudiant).
 * Table presence (V1__init.sql) : source VARCHAR(20) CHECK IN ('ETUDIANT','FORMATEUR'),
 * ajoutee_at TIMESTAMP NOT NULL DEFAULT NOW() (rempli explicitement : Hibernate ne lit pas les DEFAULT SQL).
 */
@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SourcePresence source;

    @Column(name = "ajoutee_at", nullable = false)
    private LocalDateTime ajouteeAt;

    public Presence() {
    }

    public Presence(Session session, Etudiant etudiant, SourcePresence source, LocalDateTime ajouteeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.ajouteeAt = ajouteeAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public SourcePresence getSource() {
        return source;
    }

    public void setSource(SourcePresence source) {
        this.source = source;
    }

    public LocalDateTime getAjouteeAt() {
        return ajouteeAt;
    }

    public void setAjouteeAt(LocalDateTime ajouteeAt) {
        this.ajouteeAt = ajouteeAt;
    }
}
