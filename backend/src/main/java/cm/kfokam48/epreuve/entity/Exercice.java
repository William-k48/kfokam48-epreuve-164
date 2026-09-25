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
 * Exercice déposé par un étudiant dans une session, avec relecteur optionnel (décision A2).
 * Table exercice (V1__init.sql) : lien VARCHAR(500), statut VARCHAR(20)
 * CHECK IN ('EN_ATTENTE','RELUE'), depose_at TIMESTAMP NOT NULL DEFAULT NOW(),
 * relecteur_id BIGINT NULL (UNIQUE(session_id, etudiant_id)).
 */
@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(name = "lien", nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private LocalDateTime deposeAt;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id", nullable = true)
    private Etudiant relecteur;

    public Exercice() {
    }

    public Exercice(Session session, Etudiant etudiant, String lien, StatutExercice statut,
                    LocalDateTime deposeAt, Etudiant relecteur) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.statut = statut;
        this.deposeAt = deposeAt;
        this.relecteur = relecteur;
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

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public void setStatut(StatutExercice statut) {
        this.statut = statut;
    }

    public LocalDateTime getDeposeAt() {
        return deposeAt;
    }

    public void setDeposeAt(LocalDateTime deposeAt) {
        this.deposeAt = deposeAt;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public void setRelecteur(Etudiant relecteur) {
        this.relecteur = relecteur;
    }
}
