package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import map.Map;
import entity.bullet.Bullet;
import entity.player.Player;
import types.TextureType;
import world.World;
import entity.WorldEntity;

public class WorldView implements Renderable {

    private final MapView mapView;
    private final List<SimpleView<? extends WorldEntity>> views = new CopyOnWriteArrayList<>();
    private final OrthographicCamera camera;

    public WorldView(World<?> world, Map map, OrthographicCamera camera, Player localPlayer, AssetManager assetManager) {
        this.mapView = new MapView(map, assetManager);
        this.camera = camera;
        views.add(new SimpleView<>(localPlayer, assetManager.get(TextureType.PLAYER.getName())));

        world.subscribeOnPlayerAdded(newPlayer -> views.add(new SimpleView<Player>(newPlayer, assetManager.get(TextureType.PLAYER.getName()))));
        world.subscribeOnBulletAdded(newBullet -> views.add(new SimpleView<Bullet>(newBullet, assetManager.get(TextureType.BULLET.getName()))));
        world.subscribeOnBulletRemoved(bulletID -> views.removeIf(view -> view.getId().equals(bulletID)));
    }

    @Override
    public void render(SpriteBatch batch) {
        mapView.setView(camera);
        mapView.render(batch);
        views.forEach(view -> view.render(batch));
    }
}
