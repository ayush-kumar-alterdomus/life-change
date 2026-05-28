CREATE TABLE notifications_log (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    type    VARCHAR(50) NOT NULL,
    title   VARCHAR(255) NOT NULL,
    message TEXT,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    read_at TIMESTAMP,

    CONSTRAINT fk_notifications_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
