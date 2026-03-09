CREATE TABLE credential (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
    user_id BINARY(16) NOT NULL ,
    password_hash varchar(100) NOT NULL ,
    provider VARCHAR(50) NOT NULL ,

    CONSTRAINT fk_credential_user_account
        FOREIGN KEY (user_id)
            REFERENCES user_account(id)
            ON DELETE CASCADE


)