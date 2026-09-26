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
 * Assignation d'un relecteur à un exercice (RG5 v2, décision A9) :
 * au plus DEUX relecteurs distincts par exercice, une ligne par couple
 * (exercice, relecteur). Remplace la colonne exercice.relecteur_id (V3).
 * Table assignation_relecture (V3__deux_relecteurs.sql) :
 * UNIQUE(exercice_id, relecteur_id), assignee_at TIMESTAMP NOT NULL.
 */
@Entity
@Table(name = "assignation_relecture",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_assignation_exercice_relecteur",
               columnNames = {"exercice_id", "relecteur_id"}))
public class AssignationRelecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Column(name = "assignee_at", nullable = false)
    private LocalDateTime assigneeAt;

    public AssignationRelecture() {
    }

    public AssignationRelecture(Exercice exercice, Etudiant relecteur, LocalDateTime assigneeAt) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.assigneeAt = assigneeAt;
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

    public LocalDateTime getAssigneeAt() {
        return assigneeAt;
    }

    public void setAssigneeAt(LocalDateTime assigneeAt) {
        this.assigneeAt = assigneeAt;
    }
}
