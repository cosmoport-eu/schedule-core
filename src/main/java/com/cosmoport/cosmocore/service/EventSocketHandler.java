package com.cosmoport.cosmocore.service;

import com.cosmoport.cosmocore.events.FireUpGateMessage;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.events.SyncTimetablesMessage;
import com.cosmoport.cosmocore.events.TimeoutUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EventSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final NodesHolder nodesHolder;

    public EventSocketHandler(NodesHolder nodesHolder) {
        this.nodesHolder = nodesHolder;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        sessions.add(session);
        log.info("Socket Connected: {}, params: {}", session.getRemoteAddress(), session.getAttributes());

        // Get what connected
        nodesHolder.incGates();
        nodesHolder.incTables();

        sendAll(":update-nodes:");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        log.info("Received TEXT message: {}", message);
        sendAll("text");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("Socket closed: [{}] {}", status.getCode(), status.getReason());

        try {
            super.afterConnectionClosed(session, status);
        } catch (Exception e) {
            log.error("Socket closed with an error", e);
        } finally {
            // Get what connected
            nodesHolder.decGates();
            nodesHolder.decTables();

            sendAll(":update-nodes:");
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("{}", session, exception);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("Pong {}, {}", message, session);
    }
    
    @EventListener
    public void onReloadMessage(final ReloadMessage message) {
        log.info("[socket] {}", ReloadMessage.TOKEN);
        sendAll(ReloadMessage.TOKEN);
    }

    @EventListener
    public void onTimeoutUpdateMessage(final TimeoutUpdateMessage message) {
        log.info("[socket] {}", TimeoutUpdateMessage.TOKEN);
        sendAll(TimeoutUpdateMessage.TOKEN);
    }

    @EventListener
    public void onSyncTimetablesMessage(final SyncTimetablesMessage message) {
        log.info("[socket] {}", SyncTimetablesMessage.TOKEN);
        sendAll(SyncTimetablesMessage.TOKEN);
    }

    @EventListener
    public void onFireGateMessage(final FireUpGateMessage message) {
        log.info("[socket] {}", message.toString());
        sendAll(message.toString());
    }

    /**
     * Send a message to all websocket clients.
     *
     * @param message A message to send.
     * @since 0.1.0
     */
    private void sendAll(final String message) {
        new HashSet<>(sessions).stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        log.error("[socket] Couldn't send a message {} for {}", message, session.getRemoteAddress());
                    }
                });

    }
}