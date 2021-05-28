package service;

import java.util.UUID;

import entity.player.PlayerInput;

public interface ClientRequestHandler {
    void onClientRequest(long sequenceNumber, long timestamp, UUID id, PlayerInput playerInput);
}
