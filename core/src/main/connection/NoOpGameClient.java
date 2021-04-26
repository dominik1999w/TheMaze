package connection;

import entity.player.Player;
import world.World;

public class NoOpGameClient implements GameClient {
    @Override
    public void connect() {
    }

    @Override
    public void syncState() {

    }

    @Override
    public void enterGame(Player player, World world) {

    }
}
