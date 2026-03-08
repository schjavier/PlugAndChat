CREATE TABLE guest (
    id VARCHAR(50) NOT NULL PRIMARY KEY ,
    tenant_id VARCHAR(50) NOT NULL ,
    name VARCHAR(50) NOT NULL ,
    email VARCHAR(50),
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_guest_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
)