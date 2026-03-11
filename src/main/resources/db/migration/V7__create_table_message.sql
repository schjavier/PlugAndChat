CREATE TABLE message (
    id BINARY(16) NOT NULL PRIMARY KEY,
    room_id BINARY(16) NOT NULL ,
    sender_agent_id BINARY(16),
    sender_guest_id BINARY(16),
    content TEXT NOT NULL ,
    send_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_message_agent FOREIGN KEY (sender_agent_id) REFERENCES agent(id),
    CONSTRAINT fk_message_guest FOREIGN KEY (sender_guest_id) REFERENCES guest(id),

    CONSTRAINT chk_message_sender CHECK (
        (sender_agent_id IS NOT NULL AND sender_guest_id IS NULL) OR
        (sender_agent_id IS NULL AND sender_guest_id IS NOT NULL)
    )


)