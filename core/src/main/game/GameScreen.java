package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import input.IPlayerInput;
import loader.GameLoader;
import renderable.Tile;
import renderable.TileMap;
import ui.GameUI;

public class GameScreen extends ScreenAdapter {
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final TileMap tileMap;
    private final GameUI gameUI;

    public GameScreen(SpriteBatch batch, GameLoader loader) {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.translate((float) Gdx.graphics.getWidth() / 2,
                (float) Gdx.graphics.getHeight() / 2);

        int mapWidth = 20; // temporary: number of boxes horizontal-wise
        float c = mapWidth * Tile.width / (float) Gdx.graphics.getWidth(); // temporary

        camera.zoom = c;
        camera.update();

        this.batch = batch;
        this.tileMap = loader.getTileMap();

        this.gameUI = new GameUI();
        this.gameUI.build();
    }

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        IPlayerInput playerInput = gameUI.getPlayerInput();
        if (playerInput.isShootPressed()) System.out.println("SHOOT");
        if (playerInput.getX() != 0) System.out.println("X: " + playerInput.getX());
        if (playerInput.getY() != 0) System.out.println("Y: " + playerInput.getY());

        // render the world
        Gdx.gl.glClearColor(0, 1, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        tileMap.setView(camera);
        tileMap.render(batch);

        gameUI.render(delta);

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
