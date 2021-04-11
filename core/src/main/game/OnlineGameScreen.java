package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import connection.GrpcClient;
import experimental.World;
import experimental.WorldView;
import lib.connection.ConnectReply;
import map.Map;
import map.config.MapConfig;
import map.generator.MapGenerator;
import map.mapobjects.Player;
import renderable.MapView;
import renderable.PlayerView;
import types.TextureType;
import ui.GameUI;
import util.Point2D;

public class OnlineGameScreen extends ScreenAdapter {

    private final GrpcClient client;
    private int frameCounter = 0;

    private static final String HOST =
            "10.0.2.2"
            //"localhost"
            //"10.232.0.13"
            ;

    private static final int PORT =
            50051
            //8080
            ;

    private final WorldView worldView;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final MapView mapView;
    private final PlayerView playerView;
    private final GameUI gameUI;
    private final AssetManager assetManager;

    public OnlineGameScreen(SpriteBatch batch) {
        World world = new World();
        this.worldView = new WorldView(world);
        this.client = new GrpcClient(null, world, HOST, PORT);
        int seed = this.client.connect().getSeed();

        MapGenerator mapGenerator = new MapGenerator(seed);
        Map map = mapGenerator.generateMap();

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = batch;
        this.assetManager = new AssetManager();
        for (TextureType textureType : TextureType.values())
            assetManager.load(textureType.getName(), Texture.class);
        assetManager.finishLoading();
        this.mapView = new MapView(map, assetManager);
        this.playerView = new PlayerView(new Player(new Point2D(3, 2), map),
                assetManager.get(TextureType.PLAYER.getName()));
        this.gameUI = new GameUI();

        camera.translate((float) Gdx.graphics.getWidth() / 2,
                (float) Gdx.graphics.getHeight() / 2);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        float c = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth(); // temporary

        camera.zoom = c;
        camera.update();

        gameUI.build();

        this.client.setPlayer(this.playerView.getPlayer());
    }

    @Override
    public void show() {
        ConnectReply connect = client.connect();
        this.gameUI.setDebugText("" + connect.getCount() + " " + connect.getSeed());
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

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        playerView.updateFromInput(gameUI.getPlayerInput(), delta);

        Point2D playerPosition = playerView.getPlayer().getPosition();
        camera.position.set(new Vector2(playerPosition.x(), playerPosition.y()), 0);
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

        if (frameCounter % 5 == 0) client.syncGameState();
        frameCounter++;
    }

    private void extraRender(SpriteBatch batch) {
        worldView.render(batch);
    }
}
