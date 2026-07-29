package com.nastolka.service;

import com.nastolka.dto.ChatMessageResponse;
import com.nastolka.dto.SendChatMessageRequest;

import java.util.List;

public interface LocationChatService {

    List<ChatMessageResponse> getMessages(Long locationId, String username);

    ChatMessageResponse sendMessage(Long locationId, SendChatMessageRequest request, String username);
}
