package connection;

import java.util.Random;

import entity.player.Player;
import lib.connection.ConnectReply;
import world.World;

public class NoOpClient implements GameClient {
    @Override
    public int connect() {
        return new Random().nextInt();
    }

    @Override
    public void syncGameState() {

    }

    @Override
    public void enterGame(Player player, World world) {

    }
}
