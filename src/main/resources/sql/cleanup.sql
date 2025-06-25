-- Script pour nettoyer les doublons de courses
-- Garde seulement les 3 premières courses (une de chaque ville)

DELETE FROM course WHERE id > 3;

-- Reset l'auto-increment
ALTER TABLE course ALTER COLUMN id RESTART WITH 4; 