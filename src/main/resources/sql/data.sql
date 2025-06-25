-- Insertion des utilisateurs de test
INSERT INTO users (first_name, last_name, email, password, role) VALUES 
('Admin', 'Admin', 'admin@colorrun.com', 'admin123', 'ADMIN'),
('Dupont', 'Jean', 'jean@example.com', 'jean123', 'PARTICIPANT'),
('Martin', 'Sophie', 'sophie@example.com', 'sophie123', 'ORGANISATEUR'),
('Nathanael', 'Dupont', 'nath@exemple.com', 'nath123', 'PARTICIPANT');

-- Insertion des courses de test
INSERT INTO course (nom, description, date_heure, lieu, distance, max_participants, prix, avec_obstacles, cause_soutenue) VALUES 
('ColorRun Paris', 'Course coloree dans les rues de Paris', '2025-06-15 10:00:00', 'Paris', 5, 1000, 35.0, true, 'Association A'),
('ColorRun Lyon', 'Course coloree dans les rues de Lyon', '2025-07-20 09:00:00', 'Lyon', 10, 800, 40.0, false, 'Association B'),
('ColorRun Marseille', 'Course coloree sur la plage de Marseille', '2025-08-05 08:00:00', 'Marseille', 7, 1200, 30.0, true, 'Association C');