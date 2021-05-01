package connection.game;

import java.util.UUID;

import entity.player.PlayerInput;

public class NoOpGameClient implements GameClient {
    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {

    }

    @Override
    public void syncState(long sequenceNumber, PlayerInput playerInput) {

    }

    @Override
    public void connect(UUID id) {

    }
}
