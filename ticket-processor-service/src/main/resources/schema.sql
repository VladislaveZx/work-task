CREATE TABLE IF NOT EXISTS processed_tickets (
                                                 id UUID PRIMARY KEY,
                                                 title VARCHAR(255) NOT NULL,
                                                 description TEXT,
                                                 category VARCHAR(50) NOT NULL,
                                                 status VARCHAR(50) NOT NULL,
                                                 created_at TIMESTAMP NOT NULL,
                                                 priority VARCHAR(50) NOT NULL,
                                                 sla_hours INTEGER NOT NULL,
                                                 processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_processed_tickets_status ON processed_tickets(status);
CREATE INDEX idx_processed_tickets_category ON processed_tickets(category);
CREATE INDEX idx_processed_tickets_processed_at ON processed_tickets(processed_at);
