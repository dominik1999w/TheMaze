package experimental;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import player.RemotePlayer;
import renderable.Renderable;

public class WorldView implements Renderable {

    private final World world;

    private final List<RemotePlayerView> playerViews = new CopyOnWriteArrayList<>();

    public WorldView(World world) {
        this.world = world;

        world.subscribe(newPlayer -> playerViews.add(new RemotePlayerView(newPlayer)));
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        playerViews.forEach(playerView -> playerView.render(spriteBatch));
    }
}
