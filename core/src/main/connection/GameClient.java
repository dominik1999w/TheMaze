package connection;

import entity.player.GameInputListener;
import entity.player.controller.AuthoritativePlayerController;
import world.World;

public interface GameClient extends Client, GameInputListener {
    void enterGame(World<AuthoritativePlayerController> world);
}
