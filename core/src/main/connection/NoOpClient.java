package connection;

import java.util.Random;

import lib.connection.ConnectReply;
import map.mapobjects.OPlayer;
import world.World;

public class NoOpClient implements GameClient {
    @Override
    public ConnectReply connect() {
        return ConnectReply.newBuilder()
                .setSeed(new Random().nextInt())
                .setCount(1)
                .build();
    }

    @Override
    public void syncGameState() {

    }

    @Override
    public void enterGame(OPlayer player, World world) {

    }
}
