CREATE TABLE agent (
    id BINARY(16) PRIMARY KEY NOT NULL,
    user_id BINARY(16) NOT NULL ,
    department VARCHAR(50) ,
    display_name VARCHAR(50) NOT NULL UNIQUE,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_agent_user_account FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE


)