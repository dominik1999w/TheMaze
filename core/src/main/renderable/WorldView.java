package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import map.Map;
import map.mapobjects.OPlayer;
import types.TextureType;
import world.World;

public class WorldView implements Renderable {

    private final World world;

    private final MapView mapView;
    private final PlayerView playerView;
    private final List<RemotePlayerView> playerViews = new CopyOnWriteArrayList<>();

    private final OrthographicCamera camera;

    public WorldView(World world, Map map, OPlayer player, OrthographicCamera camera, AssetManager assetManager) {
        this.world = world;
        this.camera = camera;
        this.mapView = new MapView(map, assetManager);
        this.playerView = new PlayerView(player, assetManager.get(TextureType.PLAYER.getName()), assetManager);

        world.subscribe(newPlayer -> playerViews.add(new RemotePlayerView(newPlayer,
                assetManager.get(TextureType.PLAYER.getName()))));
    }

    @Override
    public void render(SpriteBatch batch) {
        mapView.setView(camera);
        mapView.render(batch);
        playerView.render(batch);
        playerViews.forEach(playerView -> playerView.render(batch));
    }
}
