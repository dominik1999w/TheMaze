package connection.game;

import java.util.Collection;
import java.util.UUID;

import entity.bullet.Bullet;
import entity.player.Player;

public interface ServerResponseHandler {
    void onActivePlayers(Collection<UUID> playerIDs);
    void onActiveBullets(Collection<UUID> activeBullets);

    void onPlayerState(long sequenceNumber, Player playerState);
    void onBulletState(UUID shooterID, Bullet bulletState);
}
