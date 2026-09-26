-- ============================================================
-- V3 — Passage à deux relecteurs par exercice (itération 3, décision A9)
-- RG5 v2 : chaque exercice est relu par deux relecteurs différents.
-- RG9 v2 : une relecture par couple (exercice, relecteur) ; la note
--          retenue est la moyenne des relectures rendues, provisoire
--          tant qu'un seul des deux relecteurs a rendu.
--
-- Aucune migration existante n'est modifiée (B5) : V1 et V2 restent
-- telles quelles, V3 applique les changements de schéma ci-dessous.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Table d'assignation : au plus 2 relecteurs distincts par exercice
--    (remplace la colonne exercice.relecteur_id, un seul relecteur)
-- ------------------------------------------------------------
CREATE TABLE assignation_relecture (
    id            BIGSERIAL PRIMARY KEY,
    exercice_id   BIGINT       NOT NULL,
    relecteur_id  BIGINT       NOT NULL,
    assignee_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_assignation_exercice
        FOREIGN KEY (exercice_id) REFERENCES exercice (id),
    CONSTRAINT fk_assignation_relecteur
        FOREIGN KEY (relecteur_id) REFERENCES etudiant (id),
    CONSTRAINT uq_assignation_exercice_relecteur
        UNIQUE (exercice_id, relecteur_id)
    -- Des relecteurs DISTINCTS : garantis par UNIQUE(exercice_id, relecteur_id).
    -- La limite « au plus 2 par exercice » (RG5 v2) est contrôlée en service.
);

CREATE INDEX idx_assignation_exercice ON assignation_relecture (exercice_id);
CREATE INDEX idx_assignation_relecteur ON assignation_relecture (relecteur_id);

-- ------------------------------------------------------------
-- 2. Relecture : une par couple (exercice, relecteur) au lieu d'une
--    par exercice (RG9 v2). L'ancienne UNIQUE(exercice_id) est remplacée.
-- ------------------------------------------------------------
ALTER TABLE relecture DROP CONSTRAINT uq_relecture_exercice;
ALTER TABLE relecture
    ADD CONSTRAINT uq_relecture_exercice_relecteur
    UNIQUE (exercice_id, relecteur_id);

CREATE INDEX idx_relecture_exercice_relecteur ON relecture (exercice_id, relecteur_id);

-- ------------------------------------------------------------
-- 3. La colonne exercice.relecteur_id (mono-relecteur) est transférée
--    dans assignation_relecture puis supprimée.
-- ------------------------------------------------------------
INSERT INTO assignation_relecture (exercice_id, relecteur_id, assignee_at)
SELECT e.id, e.relecteur_id, e.depose_at
FROM exercice e
WHERE e.relecteur_id IS NOT NULL;

ALTER TABLE exercice DROP CONSTRAINT fk_exercice_relecteur;
DROP INDEX IF EXISTS idx_exercice_relecteur;
ALTER TABLE exercice DROP COLUMN relecteur_id;
