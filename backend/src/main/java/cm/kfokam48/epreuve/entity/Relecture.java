package cm.kfokam48.epreuve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Relecture définitive d'un exercice (RG9 : une seule par exercice, décision A1).
 * Table relecture (V1__init.sql) : exercice_id UNIQUE, relecteur_id NOT NULL,
 * note INTEGER CHECK 0-20 (RG8), commentaire TEXT, rendue_at TIMESTAMP.
 */
@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Column(name = "note", nullable = false)
    private Integer note;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "rendue_at", nullable = false)
    private LocalDateTime rendueAt;

    public Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur, Integer note, String commentaire,
                     LocalDateTime rendueAt) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = rendueAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public void setRelecteur(Etudiant relecteur) {
        this.relecteur = relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getRendueAt() {
        return rendueAt;
    }

    public void setRendueAt(LocalDateTime rendueAt) {
        this.rendueAt = rendueAt;
    }
}
