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
import java.time.LocalDateTime;

/**
 * Session de cours ouverte par un formateur, avec code de présence à 6 caractères.
 * Table session (V1__init.sql) : titre VARCHAR(200), code VARCHAR(6),
 * ouverture_at/expiration_at TIMESTAMP, cloturee BOOLEAN NOT NULL DEFAULT FALSE.
 */
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private LocalDateTime ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationAt;

    @Column(name = "cloturee", nullable = false)
    private Boolean cloturee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    public Session() {
    }

    public Session(String titre, String code, LocalDateTime ouvertureAt, LocalDateTime expirationAt,
                   Boolean cloturee, Promotion promotion) {
        this.titre = titre;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
        this.cloturee = cloturee;
        this.promotion = promotion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getOuvertureAt() {
        return ouvertureAt;
    }

    public void setOuvertureAt(LocalDateTime ouvertureAt) {
        this.ouvertureAt = ouvertureAt;
    }

    public LocalDateTime getExpirationAt() {
        return expirationAt;
    }

    public void setExpirationAt(LocalDateTime expirationAt) {
        this.expirationAt = expirationAt;
    }

    public Boolean getCloturee() {
        return cloturee;
    }

    public void setCloturee(Boolean cloturee) {
        this.cloturee = cloturee;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }
}
