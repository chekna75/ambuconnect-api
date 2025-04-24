-- Suppression du schéma et de toutes ses dépendances
DROP SCHEMA IF EXISTS ambuconnectdb CASCADE;

-- Création du nouveau schéma
CREATE SCHEMA ambuconnectdb
    AUTHORIZATION postgres;

-- Activer l'extension pour générer des UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA ambuconnectdb;

-- Spécifier le schéma
SET search_path TO ambuconnectdb;

-- Tables de base (sans dépendances)
CREATE TABLE ambuconnectdb.roles (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) UNIQUE
);

CREATE TABLE ambuconnectdb.entreprises (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    siret VARCHAR(14) UNIQUE,
    adresse VARCHAR(255),
    code_postal VARCHAR(10),
    telephone VARCHAR(20),
    email VARCHAR(255)
);

CREATE TABLE ambuconnectdb.patient (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(255),
    adresse VARCHAR(255) NOT NULL,
    code_postal NUMERIC NOT NULL,
    email VARCHAR(255),
    information TEXT,
    info_batiment TEXT,
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id) NOT NULL
);

-- Création des tables de base
CREATE TABLE ambuconnectdb.administrateurs (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id) NOT NULL,
    role_id UUID REFERENCES ambuconnectdb.roles(id) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE ambuconnectdb.chauffeurs (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    code_postal VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    num_permis VARCHAR(255) NOT NULL,
    disponible BOOLEAN NOT NULL,
    indic_actif BOOLEAN NOT NULL,
    numero_securite_sociale VARCHAR(15) NOT NULL,
    matricule VARCHAR(255) NOT NULL,
    date_entree DATE,
    niveau_convention VARCHAR(255),
    type_contrat VARCHAR(50),
    role_id UUID REFERENCES ambuconnectdb.roles(id) NOT NULL,
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id)
);

-- Tables liées aux véhicules
CREATE TABLE ambuconnectdb.ambulances (
    id UUID PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    nom VARCHAR(255),
    email VARCHAR(255),
    adresse VARCHAR(255),
    siret VARCHAR(255),
    telephone VARCHAR(20),
    marque VARCHAR(50),
    modele VARCHAR(50),
    date_achat VARCHAR(255),
    statut VARCHAR(50),
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id)
);

CREATE TABLE ambuconnectdb.vehicles (
    id UUID PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(50) NOT NULL,
    date_mise_en_service DATE,
    ambulance_id UUID REFERENCES ambuconnectdb.ambulances(id)
);

CREATE TABLE ambuconnectdb.equipments (
    id UUID PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    date_expiration DATE,
    quantite INTEGER,
    derniere_maintenance DATE,
    prochaine_maintenance DATE,
    frequence_maintenance_jours INTEGER,
    seuil_alerte_expiration_jours INTEGER,
    date_creation TIMESTAMP NOT NULL,
    date_modification TIMESTAMP NOT NULL,
    modifie_par VARCHAR(50) NOT NULL,
    ambulance_id UUID REFERENCES ambuconnectdb.ambulances(id)
);

CREATE TABLE ambuconnectdb.maintenances (
    id UUID PRIMARY KEY,
    date_entretien DATE NOT NULL,
    date_prochain_entretien DATE NOT NULL,
    type_entretien VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    vehicle_id UUID REFERENCES ambuconnectdb.vehicles(id)
);

-- Tables liées aux ressources humaines
CREATE TABLE ambuconnectdb.contrats (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    type_contrat VARCHAR(255) NOT NULL,
    salaire DOUBLE PRECISION NOT NULL
);

CREATE TABLE ambuconnectdb.conges (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    motif VARCHAR(255) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    commentaire VARCHAR(255) NOT NULL,
    date_creation DATE NOT NULL
);

CREATE TABLE ambuconnectdb.demandes_conge (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(50) NOT NULL,
    commentaire VARCHAR(255)
);

CREATE TABLE ambuconnectdb.absences (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    date_demande DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    motif VARCHAR(255),
    commentaire_validation VARCHAR(255),
    date_validation DATE
);

CREATE TABLE ambuconnectdb.fiches_paie (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id) NOT NULL,
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id) NOT NULL,
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    is_forfait_jour BOOLEAN,
    forfait_journalier DECIMAL,
    salaire_base DECIMAL NOT NULL,
    heures_travaillees DOUBLE PRECISION,
    heures_supplementaires DOUBLE PRECISION,
    taux_horaire DECIMAL,
    prime_anciennete DECIMAL,
    indemnite_transport DECIMAL,
    indemnite_repas DECIMAL,
    cotisation_maladie DECIMAL,
    cotisation_securite_sociale DECIMAL,
    cotisation_retraite DECIMAL,
    cotisation_chomage DECIMAL,
    cotisation_agff DECIMAL,
    contribution_csg DECIMAL,
    contribution_crds DECIMAL,
    total_brut DECIMAL NOT NULL,
    total_cotisations DECIMAL NOT NULL,
    net_imposable DECIMAL NOT NULL,
    net_a_payer DECIMAL NOT NULL,
    date_creation DATE NOT NULL,
    statut VARCHAR(50),
    cumul_brut_annuel DECIMAL,
    cumul_net_imposable DECIMAL,
    cumul_net_a_payer DECIMAL,
    archive BOOLEAN,
    date_archivage DATE
);

-- Tables liées aux opérations
CREATE TABLE ambuconnectdb.planning (
    id UUID PRIMARY KEY,
    date DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id),
    statut VARCHAR(50)
);

CREATE TABLE ambuconnectdb.courses (
    id UUID PRIMARY KEY,
    date_heure_depart TIMESTAMP,
    adresse_depart VARCHAR(255),
    adresse_arrivee VARCHAR(255) NOT NULL,
    distance DECIMAL,
    date_heure_arrivee TIMESTAMP,
    informations_supplementaires TEXT,
    information_patient TEXT,
    information_courses TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    temps_trajet_estime INTEGER,
    temps_trajet_reel INTEGER,
    distance_estimee DECIMAL,
    latitude_depart DOUBLE PRECISION,
    longitude_depart DOUBLE PRECISION,
    latitude_arrivee DOUBLE PRECISION,
    longitude_arrivee DOUBLE PRECISION,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id),
    ambulance_id UUID REFERENCES ambuconnectdb.ambulances(id),
    statut VARCHAR(50),
    planning_id UUID REFERENCES ambuconnectdb.planning(id),
    patient_id UUID REFERENCES ambuconnectdb.patient(id),
    entreprise_id UUID REFERENCES ambuconnectdb.entreprises(id)
);

-- Tables liées au suivi et à la communication
CREATE TABLE ambuconnectdb.localisation (
    id UUID PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    date_heure TIMESTAMP,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id)
);

CREATE TABLE ambuconnectdb.messages (
    id UUID PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_heure VARCHAR(255) NOT NULL,
    expediteur_type VARCHAR(50) NOT NULL,
    destinataire_type VARCHAR(50),
    expediteur_id UUID,
    destinataire_id UUID,
    course_id UUID REFERENCES ambuconnectdb.courses(id)
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    sender_id UUID NOT NULL,                -- L'ID de l'expéditeur (peut être un admin ou un chauffeur)
    receiver_id UUID NOT NULL,              -- L'ID du destinataire (peut être un admin ou un chauffeur)
    content TEXT NOT NULL,                    -- Le contenu du message
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Horodatage du message
    is_read BOOLEAN NOT NULL DEFAULT FALSE,   -- Statut de lecture du message
    sender_type VARCHAR(50) NOT NULL,         -- Type d'expéditeur (admin ou chauffeur)
    receiver_type VARCHAR(50) NOT NULL,       -- Type de destinataire (admin ou chauffeur)
    
    -- On ne fait plus de relation directe avec les tables admin et chauffeur
    -- Les types seront gérés avec le `sender_type` et `receiver_type`
    
    -- On laisse les messages interconnectés sans contrainte de clé étrangère sur les utilisateurs
);

-- Index pour améliorer la performance des requêtes sur les destinataires et expéditeurs
CREATE INDEX IF NOT EXISTS idx_messages_sender_receiver ON messages (sender_id, receiver_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_sender ON messages (receiver_id, sender_id);



CREATE TABLE ambuconnectdb.notifications (
    id UUID PRIMARY KEY,
    message TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    destinataire_id UUID NOT NULL,
    course_id UUID REFERENCES ambuconnectdb.courses(id),
    date_creation TIMESTAMP NOT NULL,
    lue BOOLEAN NOT NULL
);

CREATE TABLE ambuconnectdb.performances_chauffeurs (
    id UUID PRIMARY KEY,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id),
    date_debut TIMESTAMP,
    date_fin TIMESTAMP,
    heures_travaillees DOUBLE PRECISION,
    nombre_courses INTEGER,
    nombre_retards INTEGER,
    note_moyenne_feedback DOUBLE PRECISION,
    commentaires TEXT
);

CREATE TABLE ambuconnectdb.fuel_consumptions (
    id UUID PRIMARY KEY,
    date_trajet TIMESTAMP NOT NULL,
    kilometres_parcourus DOUBLE PRECISION NOT NULL,
    litres_carburant DOUBLE PRECISION NOT NULL,
    lieu_depart VARCHAR(255) NOT NULL,
    lieu_arrivee VARCHAR(255) NOT NULL,
    vehicle_id UUID REFERENCES ambuconnectdb.vehicles(id) NOT NULL
);

CREATE TABLE ambuconnectdb.attributions_vehicules (
    id UUID PRIMARY KEY,
    vehicule_id UUID REFERENCES ambuconnectdb.vehicles(id) NOT NULL,
    chauffeur_id UUID REFERENCES ambuconnectdb.chauffeurs(id) NOT NULL,
    date_attribution DATE NOT NULL,
    kilometrage_depart INTEGER,
    kilometrage_retour INTEGER,
    date_creation TIMESTAMP NOT NULL,
    commentaire TEXT
);

-- Création des index pour optimiser les performances
CREATE INDEX idx_chauffeur_email ON ambuconnectdb.chauffeurs(email);
CREATE INDEX idx_chauffeur_entreprise ON ambuconnectdb.chauffeurs(entreprise_id);
CREATE INDEX idx_vehicle_immat ON ambuconnectdb.vehicles(immatriculation);
CREATE INDEX idx_courses_date ON ambuconnectdb.courses(date_heure_depart);
CREATE INDEX idx_courses_chauffeur ON ambuconnectdb.courses(chauffeur_id);
CREATE INDEX idx_localisation_chauffeur ON ambuconnectdb.localisation(chauffeur_id);
CREATE INDEX idx_messages_course ON ambuconnectdb.messages(course_id);
CREATE INDEX idx_equipment_ambulance ON ambuconnectdb.equipments(ambulance_id);

-- Insertion des rôles
INSERT INTO ambuconnectdb.roles (id, nom) VALUES 
(gen_random_uuid(), 'ADMIN'),
(gen_random_uuid(), 'CHAUFFEUR'),
(gen_random_uuid(), 'MANAGER');

-- Insertion des entreprises
INSERT INTO ambuconnectdb.entreprises (id, nom, siret, adresse, code_postal, telephone, email) VALUES
(gen_random_uuid(), 'Ambulances Express', '12345678901234', '123 Rue de la Santé', '75001', '0123456789', 'contact@ambulances-express.fr'),
(gen_random_uuid(), 'Urgences Plus', '98765432109876', '456 Avenue des Secours', '69001', '0987654321', 'contact@urgences-plus.fr');

-- Récupérer les IDs générés pour les utiliser dans les insertions suivantes
DO $$
DECLARE
    admin_role_id UUID;
    chauffeur_role_id UUID;
    entreprise1_id UUID;
    entreprise2_id UUID;
BEGIN
    -- Récupérer les IDs des rôles
    SELECT id INTO admin_role_id FROM ambuconnectdb.roles WHERE nom = 'ADMIN' LIMIT 1;
    SELECT id INTO chauffeur_role_id FROM ambuconnectdb.roles WHERE nom = 'CHAUFFEUR' LIMIT 1;
    
    -- Récupérer les IDs des entreprises
    SELECT id INTO entreprise1_id FROM ambuconnectdb.entreprises WHERE nom = 'Ambulances Express' LIMIT 1;
    SELECT id INTO entreprise2_id FROM ambuconnectdb.entreprises WHERE nom = 'Urgences Plus' LIMIT 1;

    -- Insertion des ambulances avec les bons IDs d'entreprise
    INSERT INTO ambuconnectdb.ambulances (id, immatriculation, nom, marque, modele, date_achat, statut, entreprise_id) VALUES
    (gen_random_uuid(), 'AB-123-CD', 'Ambulance 1', 'Mercedes', 'Sprinter', '2023-01-01', 'EN_SERVICE', entreprise1_id),
    (gen_random_uuid(), 'EF-456-GH', 'Ambulance 2', 'Volkswagen', 'Crafter', '2023-02-01', 'EN_SERVICE', entreprise2_id);

    -- Insertion des vehicles
    INSERT INTO ambuconnectdb.vehicles (id, immatriculation, model, date_mise_en_service) VALUES
    (gen_random_uuid(), 'AB-123-CD', 'Sprinter 314', '2023-01-01'),
    (gen_random_uuid(), 'EF-456-GH', 'Crafter L3H2', '2023-02-01');

    -- Insertion des administrateurs
    INSERT INTO ambuconnectdb.administrateurs (id, nom, prenom, email, mot_de_passe, telephone, entreprise_id, role_id, actif) VALUES
    (gen_random_uuid(), 'Dubois', 'Jean', 'admin@ambulances-express.fr', 'motdepasse123', '0123456789', entreprise1_id, admin_role_id, true),
    (gen_random_uuid(), 'Martin', 'Sophie', 'admin@urgences-plus.fr', 'motdepasse456', '0987654321', entreprise2_id, admin_role_id, true);

    -- Insertion des chauffeurs
    INSERT INTO ambuconnectdb.chauffeurs (
        id, nom, prenom, telephone, adresse, code_postal, email, mot_de_passe, 
        num_permis, disponible, indic_actif, numero_securite_sociale, matricule, 
        date_entree, niveau_convention, type_contrat, role_id, entreprise_id
    ) VALUES
    (gen_random_uuid(), 'Petit', 'Pierre', '0612345678', '789 Rue des Conducteurs', '75002', 
    'pierre@ambulances-express.fr', 'chauffeur123', 'PERMIS123', true, true, '1234567890123', 
    'CH001', '2023-01-01', 'NIVEAU1', 'CDI', chauffeur_role_id, entreprise1_id),
    
    (gen_random_uuid(), 'Durand', 'Marie', '0623456789', '321 Avenue des Ambulanciers', '69002', 
    'marie@urgences-plus.fr', 'chauffeur456', 'PERMIS456', true, true, '9876543210987', 
    'CH002', '2023-02-01', 'NIVEAU2', 'CDI', chauffeur_role_id, entreprise2_id);

    -- Insertion des patients
    INSERT INTO ambuconnectdb.patient (
        id, nom, prenom, telephone, adresse, code_postal, 
        email, information, info_batiment, entreprise_id
    ) VALUES
    (gen_random_uuid(), 'Dupont', 'Jacques', '0612345678', '123 Rue du Patient', 75003, 
    'jacques@email.com', 'Dialyse 3x/semaine', 'Bâtiment A, 3ème étage', entreprise1_id),
    
    (gen_random_uuid(), 'Moreau', 'Sylvie', '0623456789', '456 Avenue du Malade', 69003, 
    'sylvie@email.com', 'Mobilité réduite', 'Résidence Les Fleurs, RDC', entreprise2_id);

END $$;

-- Insertion des équipements
INSERT INTO equipments (id, nom, type, date_expiration, quantite, derniere_maintenance, prochaine_maintenance, frequence_maintenance_jours, seuil_alerte_expiration_jours, date_creation, date_modification, modifie_par, ambulance_id) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Brancard Principal', 'MATERIEL_MEDICAL', '2024-12-31', 1, '2023-12-01', '2024-03-01', 90, 30, '2023-01-01 10:00:00', '2023-01-01 10:00:00', 'admin', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Défibrillateur', 'URGENCE', '2024-12-31', 1, '2023-12-01', '2024-03-01', 90, 30, '2023-01-01 10:00:00', '2023-01-01 10:00:00', 'admin', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb');

-- Insertion des maintenances
INSERT INTO maintenances (id, date_entretien, date_prochain_entretien, type_entretien, description, vehicle_id) VALUES
('gggggggg-gggg-gggg-gggg-gggggggggggg', '2023-12-01', '2024-03-01', 'REVISION', 'Révision complète', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
('hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh', '2023-12-15', '2024-03-15', 'CONTROLE_TECHNIQUE', 'Contrôle technique annuel', 'dddddddd-dddd-dddd-dddd-dddddddddddd');

-- Insertion du planning
INSERT INTO planning (id, date, heure_debut, heure_fin, chauffeur_id, statut) VALUES
('kkkkkkkk-kkkk-kkkk-kkkk-kkkkkkkkkkkk', '2024-01-15', '08:00', '16:00', '88888888-8888-8888-8888-888888888888', 'PLANIFIE'),
('llllllll-llll-llll-llll-llllllllllll', '2024-01-15', '09:00', '17:00', '99999999-9999-9999-9999-999999999999', 'PLANIFIE');

-- Insertion des courses
INSERT INTO courses (id, date_heure_depart, adresse_depart, adresse_arrivee, distance, date_heure_arrivee, chauffeur_id, ambulance_id, statut, planning_id, patient_id, entreprise_id) VALUES
('mmmmmmmm-mmmm-mmmm-mmmm-mmmmmmmmmmmm', '2024-01-15 09:00:00', '123 Rue du Patient', 'Hôpital Central', 15.5, '2024-01-15 09:45:00', '88888888-8888-8888-8888-888888888888', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TERMINEE', 'kkkkkkkk-kkkk-kkkk-kkkk-kkkkkkkkkkkk', 'iiiiiiii-iiii-iiii-iiii-iiiiiiiiiiii', '44444444-4444-4444-4444-444444444444'),
('nnnnnnnn-nnnn-nnnn-nnnn-nnnnnnnnnnnn', '2024-01-15 10:00:00', '456 Avenue du Malade', 'Clinique Sud', 12.3, '2024-01-15 10:35:00', '99999999-9999-9999-9999-999999999999', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'EN_COURS', 'llllllll-llll-llll-llll-llllllllllll', 'jjjjjjjj-jjjj-jjjj-jjjj-jjjjjjjjjjjj', '55555555-5555-5555-5555-555555555555');

-- Insertion des contrats
INSERT INTO contrats (id, chauffeur_id, date_debut, date_fin, type_contrat, salaire) VALUES
('oooooooo-oooo-oooo-oooo-oooooooooooo', '88888888-8888-8888-8888-888888888888', '2023-01-01', '2024-12-31', 'CDI', 2500.00),
('pppppppp-pppp-pppp-pppp-pppppppppppp', '99999999-9999-9999-9999-999999999999', '2023-02-01', '2024-12-31', 'CDI', 2600.00);

-- Insertion des congés
INSERT INTO conges (id, chauffeur_id, date_debut, date_fin, motif, statut, commentaire, date_creation) VALUES
('qqqqqqqq-qqqq-qqqq-qqqq-qqqqqqqqqqqq', '88888888-8888-8888-8888-888888888888', '2024-02-01', '2024-02-15', 'CONGES_PAYES', 'ACCEPTE', 'Congés d''hiver', '2024-01-01'),
('rrrrrrrr-rrrr-rrrr-rrrr-rrrrrrrrrrrr', '99999999-9999-9999-9999-999999999999', '2024-03-01', '2024-03-15', 'CONGES_PAYES', 'EN_ATTENTE', 'Vacances familiales', '2024-01-15');

-- Insertion des demandes de congé
INSERT INTO demandes_conge (id, chauffeur_id, date_debut, date_fin, statut, commentaire) VALUES
('ssssssss-ssss-ssss-ssss-ssssssssssss', '88888888-8888-8888-8888-888888888888', '2024-04-01', '2024-04-15', 'EN_ATTENTE', 'Vacances de Pâques'),
('tttttttt-tttt-tttt-tttt-tttttttttttt', '99999999-9999-9999-9999-999999999999', '2024-05-01', '2024-05-15', 'EN_ATTENTE', 'Congés de mai');

-- Insertion des absences
INSERT INTO absences (id, chauffeur_id, date_debut, date_fin, date_demande, type, statut, motif) VALUES
('uuuuuuuu-uuuu-uuuu-uuuu-uuuuuuuuuuuu', '88888888-8888-8888-8888-888888888888', '2024-01-10', '2024-01-11', '2024-01-09', 'MALADIE', 'VALIDEE', 'Grippe'),
('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvvvvv', '99999999-9999-9999-9999-999999999999', '2024-01-15', '2024-01-16', '2024-01-14', 'FORMATION', 'VALIDEE', 'Formation secourisme');

-- Insertion des messages
INSERT INTO messages (id, contenu, date_heure, expediteur_type, destinataire_type, expediteur_id, destinataire_id, course_id) VALUES
('wwwwwwww-wwww-wwww-wwww-wwwwwwwwwwww', 'Patient prêt pour le transport', '2024-01-15 08:45:00', 'CHAUFFEUR', 'ADMINISTRATEUR', '88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666', 'mmmmmmmm-mmmm-mmmm-mmmm-mmmmmmmmmmmm'),
('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx', 'Bien reçu, merci', '2024-01-15 08:46:00', 'ADMINISTRATEUR', 'CHAUFFEUR', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 'mmmmmmmm-mmmm-mmmm-mmmm-mmmmmmmmmmmm');

-- Insertion des notifications
INSERT INTO notifications (id, message, type, destinataire_id, course_id, date_creation, lue) VALUES
('yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy', 'Nouvelle course attribuée', 'COURSE', '88888888-8888-8888-8888-888888888888', 'mmmmmmmm-mmmm-mmmm-mmmm-mmmmmmmmmmmm', '2024-01-15 08:30:00', false),
('zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz', 'Rappel maintenance véhicule', 'MAINTENANCE', '99999999-9999-9999-9999-999999999999', null, '2024-01-15 09:00:00', false);

-- Insertion des performances chauffeurs
INSERT INTO performances_chauffeurs (id, chauffeur_id, date_debut, date_fin, heures_travaillees, nombre_courses, nombre_retards, note_moyenne_feedback) VALUES
('11111111-2222-3333-4444-555555555555', '88888888-8888-8888-8888-888888888888', '2024-01-01 00:00:00', '2024-01-31 23:59:59', 160.0, 45, 2, 4.8),
('22222222-3333-4444-5555-666666666666', '99999999-9999-9999-9999-999999999999', '2024-01-01 00:00:00', '2024-01-31 23:59:59', 155.0, 42, 1, 4.9);

-- Insertion des consommations de carburant
INSERT INTO fuel_consumptions (id, date_trajet, kilometres_parcourus, litres_carburant, lieu_depart, lieu_arrivee, vehicle_id) VALUES
('33333333-4444-5555-6666-777777777777', '2024-01-15 09:00:00', 150.5, 12.5, 'Paris', 'Versailles', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
('44444444-5555-6666-7777-888888888888', '2024-01-15 14:00:00', 120.3, 10.2, 'Lyon', 'Villeurbanne', 'dddddddd-dddd-dddd-dddd-dddddddddddd');

-- Insertion des attributions de véhicules
INSERT INTO attributions_vehicules (id, vehicule_id, chauffeur_id, date_attribution, kilometrage_depart, kilometrage_retour, date_creation, commentaire) VALUES
('55555555-6666-7777-8888-999999999999', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '88888888-8888-8888-8888-888888888888', '2024-01-15', 45000, 45150, '2024-01-15 08:00:00', 'Attribution journalière'),
('66666666-7777-8888-9999-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '99999999-9999-9999-9999-999999999999', '2024-01-15', 38000, 38120, '2024-01-15 09:00:00', 'Attribution journalière');

DO $$
DECLARE
    chauffeur1_id UUID;
    chauffeur2_id UUID;
    entreprise1_id UUID;
    entreprise2_id UUID;
    ambulance1_id UUID;
    ambulance2_id UUID;
    patient1_id UUID;
    patient2_id UUID;
BEGIN
    -- Récupérer les IDs nécessaires
    SELECT id INTO chauffeur1_id FROM ambuconnectdb.chauffeurs WHERE email = 'pierre@ambulances-express.fr' LIMIT 1;
    SELECT id INTO chauffeur2_id FROM ambuconnectdb.chauffeurs WHERE email = 'marie@urgences-plus.fr' LIMIT 1;
    SELECT id INTO entreprise1_id FROM ambuconnectdb.entreprises WHERE nom = 'Ambulances Express' LIMIT 1;
    SELECT id INTO entreprise2_id FROM ambuconnectdb.entreprises WHERE nom = 'Urgences Plus' LIMIT 1;
    SELECT id INTO ambulance1_id FROM ambuconnectdb.ambulances WHERE immatriculation = 'AB-123-CD' LIMIT 1;
    SELECT id INTO ambulance2_id FROM ambuconnectdb.ambulances WHERE immatriculation = 'EF-456-GH' LIMIT 1;
    SELECT id INTO patient1_id FROM ambuconnectdb.patient WHERE email = 'jacques@email.com' LIMIT 1;
    SELECT id INTO patient2_id FROM ambuconnectdb.patient WHERE email = 'sylvie@email.com' LIMIT 1;

    -- Insertion du planning
    INSERT INTO ambuconnectdb.planning (id, date, heure_debut, heure_fin, chauffeur_id, statut) VALUES
    (gen_random_uuid(), '2024-01-15', '08:00', '16:00', chauffeur1_id, 'PLANIFIE'),
    (gen_random_uuid(), '2024-01-15', '09:00', '17:00', chauffeur2_id, 'PLANIFIE');

    -- Récupérer les IDs du planning
    WITH new_planning AS (
        SELECT id, chauffeur_id 
        FROM ambuconnectdb.planning 
        WHERE date = '2024-01-15'
    )
    -- Insertion des courses
    INSERT INTO ambuconnectdb.courses (
        id, date_heure_depart, adresse_depart, adresse_arrivee, distance,
        date_heure_arrivee, chauffeur_id, ambulance_id, statut,
        planning_id, patient_id, entreprise_id
    )
    SELECT 
        gen_random_uuid(),
        '2024-01-15 09:00:00',
        '123 Rue du Patient',
        'Hôpital Central',
        15.5,
        '2024-01-15 09:45:00',
        np.chauffeur_id,
        CASE WHEN np.chauffeur_id = chauffeur1_id THEN ambulance1_id ELSE ambulance2_id END,
        'TERMINEE',
        np.id,
        CASE WHEN np.chauffeur_id = chauffeur1_id THEN patient1_id ELSE patient2_id END,
        CASE WHEN np.chauffeur_id = chauffeur1_id THEN entreprise1_id ELSE entreprise2_id END
    FROM new_planning np;

    -- Insertion des contrats
    INSERT INTO ambuconnectdb.contrats (id, chauffeur_id, date_debut, date_fin, type_contrat, salaire) VALUES
    (gen_random_uuid(), chauffeur1_id, '2023-01-01', '2024-12-31', 'CDI', 2500.00),
    (gen_random_uuid(), chauffeur2_id, '2023-02-01', '2024-12-31', 'CDI', 2600.00);

    -- Insertion des congés
    INSERT INTO ambuconnectdb.conges (id, chauffeur_id, date_debut, date_fin, motif, statut, commentaire, date_creation) VALUES
    (gen_random_uuid(), chauffeur1_id, '2024-02-01', '2024-02-15', 'CONGES_PAYES', 'ACCEPTE', 'Congés d''hiver', '2024-01-01'),
    (gen_random_uuid(), chauffeur2_id, '2024-03-01', '2024-03-15', 'CONGES_PAYES', 'EN_ATTENTE', 'Vacances familiales', '2024-01-15');

END $$;

-- Tables pour le module établissements de santé
CREATE TABLE ambuconnectdb.etablissements_sante (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    type_etablissement VARCHAR(50) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    email_contact VARCHAR(255) NOT NULL UNIQUE,
    telephone_contact VARCHAR(20) NOT NULL,
    responsable_referent_id UUID REFERENCES ambuconnectdb.administrateurs(id) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE ambuconnectdb.utilisateurs_etablissement (
    id UUID PRIMARY KEY,
    etablissement_id UUID REFERENCES ambuconnectdb.etablissements_sante(id) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE ambuconnectdb.demandes_transport (
    id UUID PRIMARY KEY,
    etablissement_id UUID REFERENCES ambuconnectdb.etablissements_sante(id) NOT NULL,
    created_by UUID REFERENCES ambuconnectdb.utilisateurs_etablissement(id) NOT NULL,
    patient_id UUID REFERENCES ambuconnectdb.patient(id) NOT NULL,
    adresse_depart VARCHAR(255) NOT NULL,
    adresse_arrivee VARCHAR(255) NOT NULL,
    horaire_souhaite TIMESTAMP NOT NULL,
    type_transport VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    societe_affectee_id UUID REFERENCES ambuconnectdb.entreprises(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE ambuconnectdb.configurations_etablissement (
    id UUID PRIMARY KEY,
    etablissement_id UUID REFERENCES ambuconnectdb.etablissements_sante(id) NOT NULL,
    lundi_debut TIME,
    lundi_fin TIME,
    mardi_debut TIME,
    mardi_fin TIME,
    mercredi_debut TIME,
    mercredi_fin TIME,
    jeudi_debut TIME,
    jeudi_fin TIME,
    vendredi_debut TIME,
    vendredi_fin TIME,
    samedi_debut TIME,
    samedi_fin TIME,
    dimanche_debut TIME,
    dimanche_fin TIME
);

CREATE TABLE ambuconnectdb.etablissement_societes_preferees (
    configuration_id UUID REFERENCES ambuconnectdb.configurations_etablissement(id) NOT NULL,
    societe_id UUID REFERENCES ambuconnectdb.entreprises(id) NOT NULL,
    PRIMARY KEY (configuration_id, societe_id)
);

CREATE TABLE ambuconnectdb.etablissement_tarifs_negocies (
    configuration_id UUID REFERENCES ambuconnectdb.configurations_etablissement(id) NOT NULL,
    societe_id UUID NOT NULL,
    type_transport VARCHAR(50) NOT NULL,
    tarif_base DECIMAL(10,2) NOT NULL,
    reduction_pourcentage INTEGER NOT NULL,
    PRIMARY KEY (configuration_id, societe_id, type_transport)
);

CREATE TABLE ambuconnectdb.messages_etablissement (
    id UUID PRIMARY KEY,
    etablissement_id UUID REFERENCES ambuconnectdb.etablissements_sante(id) NOT NULL,
    auteur_id UUID REFERENCES ambuconnectdb.utilisateurs_etablissement(id) NOT NULL,
    message TEXT NOT NULL,
    date_envoi TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canal VARCHAR(50) NOT NULL,
    demande_transport_id UUID REFERENCES ambuconnectdb.demandes_transport(id)
);