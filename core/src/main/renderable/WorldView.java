package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

import entity.WorldEntity;
import entity.bullet.Bullet;
import entity.player.Player;
import map.Map;
import physics.CollisionWorld;
import types.TextureType;
import world.World;

public class WorldView implements Renderable {

    private final MapView mapView;
    private final List<SimpleView<? extends WorldEntity>> views = new ArrayList<>();
    private final OrthographicCamera camera;
    private final SightView sightView;

    public WorldView(World<?> world, Map map, OrthographicCamera camera, Player localPlayer, AssetManager assetManager, CollisionWorld collisionWorld) {
        this.mapView = new MapView(map, assetManager);
        this.camera = camera;
        sightView = new SightView(map, localPlayer, map.getMapLength());
        views.add(new SimpleView<>(localPlayer, assetManager.get(TextureType.PLAYER.getName())));

        world.subscribeOnPlayerAdded(newPlayer -> views.add(new SimpleView<Player>(newPlayer, assetManager.get(TextureType.PLAYER.getName()))));
        world.subscribeOnPlayerRemoved(playerID -> views.removeIf(view -> view.getId().equals(playerID)));
        world.subscribeOnBulletAdded((shooterID, newBullet) -> views.add(new SimpleView<Bullet>(newBullet, assetManager.get(TextureType.BULLET.getName()))));
        world.subscribeOnBulletRemoved(bulletID -> views.removeIf(view -> view.getId().equals(bulletID)));
    }

    @Override
    public void render(SpriteBatch batch) {
        mapView.setView(camera);
        mapView.render(batch);
        views.forEach(view -> view.render(batch));
        sightView.render(batch);
    }
}
