CREATE TABLE tenant
(
    id      VARCHAR(50) PRIMARY KEY NOT NULL,
    name    VARCHAR(50)             NOT NULL,
    api_key VARCHAR(100)            NOT NULL UNIQUE
);