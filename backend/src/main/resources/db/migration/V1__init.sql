-- ============================================================
-- V1 — Schéma initial de l'application de présence et relecture
-- Correspond au diagramme D2 (docs/diagrammes/d2-classes.md)
-- ============================================================

-- ------------------------------------------------------------
-- Table : promotion
-- ------------------------------------------------------------
CREATE TABLE promotion (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(100) NOT NULL
);

-- ------------------------------------------------------------
-- Table : etudiant
-- ------------------------------------------------------------
CREATE TABLE etudiant (
    id            BIGSERIAL PRIMARY KEY,
    nom           VARCHAR(150) NOT NULL,
    promotion_id  BIGINT NOT NULL,
    CONSTRAINT fk_etudiant_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotion (id)
);

CREATE INDEX idx_etudiant_promotion ON etudiant (promotion_id);

-- ------------------------------------------------------------
-- Table : session
-- ------------------------------------------------------------
CREATE TABLE session (
    id             BIGSERIAL PRIMARY KEY,
    titre          VARCHAR(200) NOT NULL,
    code           VARCHAR(6)   NOT NULL,
    ouverture_at   TIMESTAMP    NOT NULL,
    expiration_at  TIMESTAMP    NOT NULL,
    cloturee       BOOLEAN      NOT NULL DEFAULT FALSE,
    promotion_id   BIGINT       NOT NULL,
    CONSTRAINT fk_session_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotion (id)
);

CREATE INDEX idx_session_code ON session (code);
CREATE INDEX idx_session_promotion ON session (promotion_id);

-- ------------------------------------------------------------
-- Table : presence
-- Contrainte RG2 : un étudiant ne peut marquer sa présence
-- qu'une seule fois par session → UNIQUE(session_id, etudiant_id)
-- Enum RG13 : source = ETUDIANT ou FORMATEUR
-- ------------------------------------------------------------
CREATE TABLE presence (
    id           BIGSERIAL PRIMARY KEY,
    session_id   BIGINT       NOT NULL,
    etudiant_id  BIGINT       NOT NULL,
    source       VARCHAR(20)  NOT NULL,
    ajoutee_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_presence_session
        FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT fk_presence_etudiant
        FOREIGN KEY (etudiant_id) REFERENCES etudiant (id),
    CONSTRAINT uq_presence_session_etudiant
        UNIQUE (session_id, etudiant_id),
    CONSTRAINT ck_presence_source
        CHECK (source IN ('ETUDIANT', 'FORMATEUR'))
);

CREATE INDEX idx_presence_session ON presence (session_id);
CREATE INDEX idx_presence_etudiant ON presence (etudiant_id);

-- ------------------------------------------------------------
-- Table : exercice
-- Contrainte : un seul exercice par couple (session, étudiant)
-- relecteur_id nullable (décision A2 : « relecteur à assigner »)
-- Enum : statut = EN_ATTENTE ou RELUE
-- ------------------------------------------------------------
CREATE TABLE exercice (
    id            BIGSERIAL PRIMARY KEY,
    session_id    BIGINT       NOT NULL,
    etudiant_id   BIGINT       NOT NULL,
    lien          VARCHAR(500) NOT NULL,
    statut        VARCHAR(20)  NOT NULL,
    depose_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    relecteur_id  BIGINT       NULL,
    CONSTRAINT fk_exercice_session
        FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT fk_exercice_etudiant
        FOREIGN KEY (etudiant_id) REFERENCES etudiant (id),
    CONSTRAINT fk_exercice_relecteur
        FOREIGN KEY (relecteur_id) REFERENCES etudiant (id),
    CONSTRAINT uq_exercice_session_etudiant
        UNIQUE (session_id, etudiant_id),
    CONSTRAINT ck_exercice_statut
        CHECK (statut IN ('EN_ATTENTE', 'RELUE'))
);

CREATE INDEX idx_exercice_session ON exercice (session_id);
CREATE INDEX idx_exercice_etudiant ON exercice (etudiant_id);
CREATE INDEX idx_exercice_relecteur ON exercice (relecteur_id);

-- ------------------------------------------------------------
-- Table : relecture
-- Contrainte RG8 : note entière de 0 à 20 → CHECK
-- Contrainte RG9 : relecture définitive → une seule par exercice
-- relecture.exercice_id est UNIQUE (un exercice = une relecture)
-- ------------------------------------------------------------
CREATE TABLE relecture (
    id            BIGSERIAL PRIMARY KEY,
    exercice_id   BIGINT       NOT NULL,
    relecteur_id  BIGINT       NOT NULL,
    note          INTEGER      NOT NULL,
    commentaire   TEXT,
    rendue_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_relecture_exercice
        FOREIGN KEY (exercice_id) REFERENCES exercice (id),
    CONSTRAINT fk_relecture_relecteur
        FOREIGN KEY (relecteur_id) REFERENCES etudiant (id),
    CONSTRAINT uq_relecture_exercice
        UNIQUE (exercice_id),
    CONSTRAINT ck_relecture_note
        CHECK (note BETWEEN 0 AND 20)
);

CREATE INDEX idx_relecture_exercice ON relecture (exercice_id);
CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);