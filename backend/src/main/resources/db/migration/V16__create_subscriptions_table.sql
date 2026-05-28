CREATE TABLE subscriptions (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID UNIQUE NOT NULL,
    provider   VARCHAR(100),
    plan_type  VARCHAR(50) NOT NULL DEFAULT 'FREE',
    premium    BOOLEAN NOT NULL DEFAULT false,
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    auto_renew BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
