package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connection.GameClient;
import world.World;
import renderable.WorldView;
import map.Map;
import map.config.MapConfig;
import map.generator.MapGenerator;
import map.mapobjects.OPlayer;
import types.TextureType;
import ui.GameUI;
import util.Point2D;

public class GameScreen extends ScreenAdapter {

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final OPlayer player;

    private final WorldView worldView;
    private final GameUI gameUI;
    private final AssetManager assetManager;

    public GameScreen(SpriteBatch batch, GameClient client) {
        this.batch = batch;
        this.assetManager = new AssetManager();
        for (TextureType textureType : TextureType.values())
            assetManager.load(textureType.getName(), Texture.class);
        assetManager.finishLoading();

        this.client = client;
        int seed = this.client.connect().getSeed();

        MapGenerator mapGenerator = new MapGenerator(seed);
        Map map = mapGenerator.generateMap();

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.player = new OPlayer(new Point2D(3, 2), map);
        World world = new World();
        this.worldView = new WorldView(world, map, player, camera, assetManager);

        this.gameUI = new GameUI();

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        float c = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth(); // temporary

        camera.zoom = c;
        camera.update();

        gameUI.build();

        this.client.enterGame(player, world);
    }

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        player.updateFromInput(gameUI.getPlayerInput(), delta);

        Point2D playerPosition = player.getPosition();
        camera.position.set(playerPosition.x(), playerPosition.y(), 0);
        camera.update();

        // render the world
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldView.render(batch);
        batch.end();

        gameUI.render(delta);

        if (frameCounter % 5 == 0) client.syncGameState();
        frameCounter++;
    }

    @Override
    public void show() {
        // FIXME: must not connect twice
        //ConnectReply connect = client.connect();
        //this.gameUI.setDebugText("" + connect.getCount() + " " + connect.getSeed());
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

    private final GameClient client;
    private int frameCounter = 0;
}
