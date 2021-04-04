package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import loader.GameLoader;
import map.Map;
import map.config.MapConfig;
import map.mapobjects.Player;
import renderable.MapView;
import renderable.PlayerView;
import ui.GameUI;

public class OfflineGameScreen extends ScreenAdapter {

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final MapView tileMap;
    final PlayerView playerView;
    final GameUI gameUI;

    public OfflineGameScreen(SpriteBatch batch, GameLoader loader, Map map) {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.translate((float) Gdx.graphics.getWidth() / 2,
                (float) Gdx.graphics.getHeight() / 2);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        float c = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth(); // temporary

        camera.zoom = c;
        camera.update();

        this.batch = batch;
        this.tileMap = loader.getTileMap();

        this.playerView = new PlayerView(new Player(new Vector2(3, 2), map));

        this.gameUI = new GameUI();
        this.gameUI.build();
    }

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        playerView.updateFromInput(gameUI.getPlayerInput(), delta);

        camera.position.set(playerView.getPosition(), 0);
        camera.update();

        // render the world
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        tileMap.setView(camera);
        tileMap.render(batch);

        gameUI.render(delta);

        batch.end();

        //TODO: why do i have to do it separately?
        batch.begin();
        playerView.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gameUI.resize(width, height);
    }

    @Override
    public void dispose() {
        gameUI.dispose();
    }

}
