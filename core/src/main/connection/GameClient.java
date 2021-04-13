package connection;

import lib.connection.ConnectReply;
import map.mapobjects.OPlayer;
import world.World;

public interface GameClient {

    ConnectReply connect();
    void syncGameState();

    void enterGame(OPlayer player, World world);
}
