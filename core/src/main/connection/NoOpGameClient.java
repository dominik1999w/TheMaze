package connection;

import java.util.UUID;

import entity.player.PlayerInput;
import entity.player.controller.LocalPlayerController;
import world.World;

public class NoOpGameClient implements GameClient {
    @Override
    public void connect() {
    }

    @Override
    public void enterGame(UUID id, ServerResponseListener responseListener) {

    }

    @Override
    public boolean syncState(long sequenceNumber, PlayerInput playerInput) {
        return false;
    }
}
