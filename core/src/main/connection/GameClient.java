package connection;

import entity.player.GameInputListener;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.LocalPlayerController;
import world.World;

public interface GameClient extends Client, GameInputListener {
    void enterGame(LocalPlayerController localPlayerController, World<AuthoritativePlayerController> world);
}
