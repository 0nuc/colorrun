-- Script pour nettoyer les doublons de courses
-- Garde seulement les 3 premières courses (une de chaque ville)

DELETE FROM course WHERE id > 3;

-- Reset l'auto-increment dynamiquement
ALTER TABLE course ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM course); 