package connection;

import entity.player.PlayerInput;
import entity.player.controller.LocalPlayerController;
import world.World;

public class NoOpGameClient implements GameClient {
    @Override
    public void connect() {
    }

    @Override
    public void syncState() {

    }

    @Override
    public void enterGame(LocalPlayerController localPlayerController, World world) {

    }

    @Override
    public void notifyInput(PlayerInput playerInput) {

    }
}
