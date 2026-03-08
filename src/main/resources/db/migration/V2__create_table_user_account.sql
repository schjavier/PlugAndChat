CREATE TABLE user_account (
    id varchar(50) NOT NULL PRIMARY KEY,
    tenant_id varchar(50) NOT NULL,
    email varchar(50),
    role varchar(20),
    is_locked boolean,
    deleted_at TIMESTAMP DEFAULT NULL,
    constraint fk_tenant_user_account FOREIGN KEY (tenant_id) REFERENCES tenant (id) ON DELETE CASCADE
);