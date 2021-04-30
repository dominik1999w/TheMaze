package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Random;

import connection.ClientFactory;
import connection.GameClient;
import connection.MapClient;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import renderable.MapView;
import types.SkinType;

public class MenuScreen extends ScreenAdapter {
    private static final String HOST =
            "10.0.2.2"
//            "localhost"
//            "10.232.0.13"
            ;

    private static final int PORT =
            50051
            //8080
            ;

    private final GameApp game;
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Stage stage;

    private Container<Table> menuContainer;
    private Container<Actor> sliderContainer;
    private Label status;

    private GameScreen gameScreen;

    private MapClient mapClient;
    private GameClient gameClient;

    private final AsyncExecutor asyncExecutor;
    private AsyncResult<Void> task;

    private MapView mapView;
    private OrthographicCamera camera;

    private final Random random = new Random();

    private final float initialZoom = 0.166f;
    private final int defaultHeight = 1080;

    private final int minMapLength = 5;
    private final int maxMapLength = 50;
    private final int defaultSeed = 0;

    public MenuScreen(GameApp game, SpriteBatch batch, AssetManager assetManager) {
        this.game = game;
        this.batch = batch;
        this.assetManager = assetManager;
        this.skin = assetManager.get(SkinType.GLASSY.getName());
        this.asyncExecutor = new AsyncExecutor(1);
        this.stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        buildUI();
        connect();
    }

    void connect() {
        task = asyncExecutor.submit(() -> {
            gameClient = ClientFactory.newGameClient(HOST, PORT);
            mapClient = ClientFactory.newMapClient(HOST, PORT);
            mapClient.connect();

            if (mapClient.isHost()) {
                Gdx.app.postRunnable(() -> sliderContainer.setVisible(true));
            }

            Gdx.app.postRunnable(() -> status.setText("server status: connected"));

            return null;
        });

    }

    void buildUI() {
        buildMenuContainer();
        buildSliderContainer();
        buildMap();

        status = new Label("server status: connecting", skin, "big");
        status.setPosition(10, 0);

        stage.addActor(status);
        stage.addActor(menuContainer);
        stage.addActor(sliderContainer);

        Gdx.input.setInputProcessor(stage);
    }

    private void buildMenuContainer() {
        float leftContainerWidth = Gdx.graphics.getWidth() * 0.45f;
        float leftContainerHeight = Gdx.graphics.getHeight() * 0.95f;

        float titleFontScale = 2.5f;
        float optionFontScale = 1.5f;

        menuContainer = new Container<>();
        menuContainer.setSize(leftContainerWidth, leftContainerHeight);
        menuContainer.setPosition(0, (Gdx.graphics.getHeight() - leftContainerHeight) / 2);
        menuContainer.fillX();
        menuContainer.setDebug(true);

        Table info = new Table();
        info.setFillParent(true);
        info.defaults().pad(10.0f);

        Label title = new Label("The Maze", skin, "big");
        title.setFontScale(titleFontScale * Gdx.graphics.getHeight() / defaultHeight);
        info.add(title);

        info.row().padTop(50.0f);

        TextButton startGame = new TextButton("Start Game", skin);
        startGame.getLabel().setFontScale(optionFontScale * Gdx.graphics.getHeight() / defaultHeight);
        startGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (task.isDone()) {
                    MapGenerator mapGenerator = new MapGenerator(mapClient.getMapLength());
                    Map map = mapGenerator.generateMap(mapClient.getSeed());
                    gameScreen = new GameScreen(batch, gameClient, map, assetManager);
                    game.setScreen(gameScreen);
                }
            }
        });
        info.add(startGame).fillX();

        info.row();

        TextButton quitGame = new TextButton("Quit", skin);
        quitGame.getLabel().setFontScale(optionFontScale * Gdx.graphics.getHeight() / defaultHeight);
        info.add(quitGame).fillX();

        menuContainer.setActor(info);
    }

    private void buildSliderContainer() {
        float rightContainerWidth = (MapConfig.BOX_SIZE) * minMapLength / camera.zoom;
        float rightContainerHeight = menuContainer.getHeight();

        sliderContainer = new Container<>();
        sliderContainer.setSize(rightContainerWidth, rightContainerHeight);
        sliderContainer.setPosition(menuContainer.getWidth(), (Gdx.graphics.getHeight() - rightContainerHeight) / 2);
        sliderContainer.fillX();
        sliderContainer.setDebug(true);
        sliderContainer.setVisible(false);

        Table config = new Table();
        config.setFillParent(true);

        final Slider slider = new Slider(minMapLength, maxMapLength, 1, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (!slider.isDragging()) {
                    return;
                }
                updateMap((int) slider.getValue(), random.nextInt());
            }
        });
        config.add(slider).growX();

        sliderContainer.setActor(config);
    }

    private void buildMap() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapView = new MapView(new MapGenerator(minMapLength).generateMap(defaultSeed), assetManager);
        updateMap(minMapLength, defaultSeed);
    }

    private void updateMap(int length, int seed) {
        if (task != null && task.isDone()) {
            mapClient.setMapLength(length);
            mapClient.setSeed(seed);
        }

        updateCamera(length);

        mapView.setMap(new MapGenerator(length).generateMap(seed));
        mapView.setView(camera);
    }

    private void updateCamera(int mapLength) {
        camera.zoom = initialZoom * defaultHeight / Gdx.graphics.getHeight() * mapLength / minMapLength;
        camera.position.set(
                camera.zoom * (0.5f * Gdx.graphics.getWidth() - menuContainer.getWidth()) + 0.5f * MapConfig.WALL_THICKNESS,
                camera.zoom * menuContainer.getHeight() * 0.5f,
                0);
        camera.update();
    }

    int prevLength = minMapLength;
    int prevSeed = defaultSeed;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (task.isDone()) {
            mapClient.syncState();
            if (!mapClient.isHost() && (prevLength != mapClient.getMapLength() || prevSeed != mapClient.getSeed())) {
                prevLength = mapClient.getMapLength();
                prevSeed = mapClient.getSeed();
                updateMap(prevLength, prevSeed);
            }
        }

        batch.begin();
        mapView.render(batch);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        asyncExecutor.dispose();
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }
}
