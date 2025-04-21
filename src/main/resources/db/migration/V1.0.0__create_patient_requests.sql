CREATE TABLE patient_requests (
    id UUID PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    pickup_address TEXT NOT NULL,
    destination_latitude DOUBLE PRECISION NOT NULL,
    destination_longitude DOUBLE PRECISION NOT NULL,
    destination_address TEXT NOT NULL,
    requested_time TIMESTAMP NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    assigned_entreprise_id UUID REFERENCES entreprises(id),
    status VARCHAR(20) NOT NULL,
    additional_notes TEXT
); 