package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import map.Map;
import map.config.MapConfig;
import map.mapobjects.Player;
import renderable.MapView;
import renderable.PlayerView;
import ui.GameUI;
import util.Point2D;

public class OfflineGameScreen extends ScreenAdapter {

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final MapView mapView;
    final PlayerView playerView;
    final GameUI gameUI;
    final AssetManager assetManager;

    public OfflineGameScreen(SpriteBatch batch, Map map) {
        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = batch;
        this.assetManager = new AssetManager();
        assetManager.load("player.png", Texture.class);
        assetManager.finishLoading();
        this.mapView = new MapView(map);
        this.playerView = new PlayerView(new Player(new Point2D(3, 2), map),
                assetManager.get("player.png"));
        this.gameUI = new GameUI();

        camera.translate((float) Gdx.graphics.getWidth() / 2,
                (float) Gdx.graphics.getHeight() / 2);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        float c = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth(); // temporary

        camera.zoom = c;
        camera.update();

        gameUI.build();
    }

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        playerView.updateFromInput(gameUI.getPlayerInput(), delta);

        Point2D playerPosition = playerView.getPlayer().getPosition();
        camera.position.set(new Vector2(playerPosition.x, playerPosition.y), 0);
        camera.update();

        // render the world
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        mapView.setView(camera);
        mapView.render(batch);
        playerView.render(batch);

        extraRender(batch);

        batch.end();

        gameUI.render(delta);
    }

    // TODO: TEMP
    protected void extraRender(SpriteBatch batch) {

    }

    @Override
    public void resize(int width, int height) {
        gameUI.resize(width, height);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        gameUI.dispose();
    }

}
