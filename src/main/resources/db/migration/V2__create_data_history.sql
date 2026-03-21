CREATE TABLE IF NOT EXISTS data_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL CHECK (user_id > 0),
    "time" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    element VARCHAR(255) NOT NULL CHECK (char_length(element) > 0),
    response VARCHAR(255) NOT NULL CHECK (char_length(response) > 0)
);