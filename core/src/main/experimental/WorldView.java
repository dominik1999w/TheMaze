package experimental;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import player.RemotePlayer;
import renderable.Renderable;
import types.TextureType;

public class WorldView implements Renderable {

    private final World world;

    private final List<RemotePlayerView> playerViews = new CopyOnWriteArrayList<>();

    public WorldView(World world, AssetManager assetManager) {
        this.world = world;

        world.subscribe(newPlayer -> playerViews.add(new RemotePlayerView(newPlayer,
                assetManager.get(TextureType.PLAYER.getName()))));
    }

    @Override
    public void render(SpriteBatch batch) {
        playerViews.forEach(playerView -> playerView.render(batch));
    }
}
