-- Suppression des tables existantes
DROP TABLE IF EXISTS chauffeur_positions CASCADE;
DROP TABLE IF EXISTS demande_prise_en_charge CASCADE;
DROP TABLE IF EXISTS informations_medicales CASCADE;
DROP TABLE IF EXISTS devis CASCADE;

-- Création de la table chauffeur_positions
CREATE TABLE chauffeur_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chauffeur_id UUID NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    precision DOUBLE PRECISION,
    vitesse DOUBLE PRECISION,
    direction DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chauffeur
        FOREIGN KEY (chauffeur_id)
        REFERENCES chauffeurs(id)
        ON DELETE CASCADE
);

-- Création de la table demande_prise_en_charge
CREATE TABLE demande_prise_en_charge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    adresse_depart VARCHAR(255) NOT NULL,
    adresse_destination VARCHAR(255) NOT NULL,
    date_prise_en_charge TIMESTAMP WITH TIME ZONE NOT NULL,
    statut VARCHAR(50) NOT NULL,
    commentaire TEXT,
    type_transport VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(id)
        ON DELETE CASCADE
);

-- Création de la table informations_medicales
CREATE TABLE informations_medicales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_securite_sociale_crypte VARCHAR(255) NOT NULL,
    type_prise_en_charge VARCHAR(50),
    mobilite_patient VARCHAR(50),
    equipements_speciaux TEXT,
    medecin_prescripteur_nom VARCHAR(255),
    medecin_prescripteur_rpps VARCHAR(50),
    etablissement_prescripteur VARCHAR(255),
    patient_id UUID UNIQUE REFERENCES patient(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Création de la table devis
CREATE TABLE devis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    demande_id UUID NOT NULL REFERENCES demande_prise_en_charge(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patient(id),
    entreprise_id UUID NOT NULL REFERENCES entreprises(id),
    date_creation TIMESTAMP WITH TIME ZONE NOT NULL,
    date_validite TIMESTAMP WITH TIME ZONE,
    montant_base DECIMAL(10,2) NOT NULL,
    montant_majoration DECIMAL(10,2),
    montant_total DECIMAL(10,2) NOT NULL,
    distance_estimee DECIMAL(10,2),
    type_transport VARCHAR(50),
    statut VARCHAR(50),
    taux_remboursement INTEGER,
    montant_remboursement DECIMAL(10,2),
    reste_a_charge DECIMAL(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Création des index pour chauffeur_positions
CREATE INDEX idx_chauffeur_positions_location ON chauffeur_positions(latitude, longitude);
CREATE INDEX idx_chauffeur_positions_chauffeur_id ON chauffeur_positions(chauffeur_id);
CREATE INDEX idx_chauffeur_positions_timestamp ON chauffeur_positions(timestamp);

-- Création des index pour demande_prise_en_charge
CREATE INDEX idx_demande_patient_id ON demande_prise_en_charge(patient_id);
CREATE INDEX idx_demande_date ON demande_prise_en_charge(date_prise_en_charge);
CREATE INDEX idx_demande_statut ON demande_prise_en_charge(statut);

-- Création des index pour informations_medicales
CREATE INDEX idx_infos_medicales_patient ON informations_medicales(patient_id);

-- Création des index pour devis
CREATE INDEX idx_devis_patient ON devis(patient_id);
CREATE INDEX idx_devis_entreprise ON devis(entreprise_id);
CREATE INDEX idx_devis_demande ON devis(demande_id);
CREATE INDEX idx_devis_statut ON devis(statut);
CREATE INDEX idx_devis_dates ON devis(date_creation, date_validite); 