package com.lotorojo.plugandchat.messaging.repository;

import com.lotorojo.plugandchat.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByRoomUuidOrderBySendDateAsc(UUID roomId);

}
