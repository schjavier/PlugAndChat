CREATE TABLE room (
    id varchar(50) NOT NULL PRIMARY KEY ,
    tenant_id varchar(50) NOT NULL ,
    guest_id varchar(50) NOT NULL ,
    agent_id varchar(50),
    status varchar(50) NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP ,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_room_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE ,
    CONSTRAINT fk_room_guest FOREIGN KEY (guest_id) REFERENCES guest(id),
    CONSTRAINT fk_room_agent FOREIGN KEY (agent_id) REFERENCES agent(id)

)