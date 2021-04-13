package connection;

import entity.player.Player;
import lib.connection.ConnectReply;
import world.World;

public interface GameClient {

    ConnectReply connect();
    void syncGameState();

    void enterGame(Player player, World world);
}
