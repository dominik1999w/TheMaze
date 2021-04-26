package connection;

import entity.player.Player;
import entity.player.controller.AuthoritativePlayerController;
import world.World;

public interface GameClient extends Client {
    void enterGame(Player player, World<AuthoritativePlayerController> world);
}
