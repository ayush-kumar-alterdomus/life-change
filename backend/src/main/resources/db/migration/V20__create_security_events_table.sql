-- Create security_events table for audit logging
CREATE TABLE security_events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type      VARCHAR(50) NOT NULL,
    ip_address      VARCHAR(45),
    details         JSONB DEFAULT '{}',
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- Indexes for common query patterns
CREATE INDEX idx_security_events_user_id ON security_events(user_id);
CREATE INDEX idx_security_events_event_type ON security_events(event_type);
CREATE INDEX idx_security_events_created_at ON security_events(created_at);
