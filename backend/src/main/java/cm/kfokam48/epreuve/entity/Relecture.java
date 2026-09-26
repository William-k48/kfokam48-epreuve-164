package cm.kfokam48.epreuve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Relecture d'un exercice rendue par un de ses relecteurs.
 * v2 (itération 3, décision A9) : un exercice possède DEUX relecteurs ;
 * il y a donc UNE relecture par couple (exercice, relecteur) —
 * UNIQUE(exercice_id, relecteur_id) depuis V3 — et la note retenue est la
 * moyenne des relectures rendues (provisoire tant qu'un seul a rendu).
 * Table relecture (V1__init.sql, modifiée par V3) : relecteur_id NOT NULL,
 * note INTEGER CHECK 0-20 (RG8), commentaire TEXT, rendue_at TIMESTAMP.
 */
@Entity
@Table(name = "relecture",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_relecture_exercice_relecteur",
               columnNames = {"exercice_id", "relecteur_id"}))
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    /**
     * v2 : le relecteur de CETTE relecture (l'un des deux relecteurs assignés
     * de l'exercice). Tient lieu d'identification sans auth (décision A10).
     */
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
