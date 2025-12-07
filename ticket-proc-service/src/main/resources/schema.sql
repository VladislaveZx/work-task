CREATE TABLE processed_tickets (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    priority VARCHAR(50) NOT NULL,
    sla_hours INT NOT NULL,
    processed_at TIMESTAMP NOT NULL

);