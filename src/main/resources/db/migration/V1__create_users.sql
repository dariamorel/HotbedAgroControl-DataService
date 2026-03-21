CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL CHECK (char_length(ip_address) > 0),
    topic VARCHAR(255) NOT NULL CHECK (char_length(topic) > 0),
    user_name VARCHAR(255) NOT NULL CHECK (char_length(user_name) > 0),
    password VARCHAR(255) NOT NULL CHECK (char_length(password) > 0),
    port INTEGER NOT NULL CHECK (port >= 0)
);