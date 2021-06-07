package connection.game;

import java.util.Collection;
import java.util.UUID;

import connection.Client;
import entity.bullet.Bullet;
import entity.player.Player;
import entity.player.PlayerInput;

public interface GameClient extends Client {
    interface ServerResponseHandler {
        void onActivePlayers(Collection<UUID> playerIDs);

        void onPlayerState(long sequenceNumber, long timestamp, Player playerState);
        void onBulletState(UUID shooterID, Bullet bulletState);
    }

    void dispatchMessages(ServerResponseHandler responseHandler);
    void syncState(long sequenceNumber, PlayerInput playerInput, boolean micActive);
}
