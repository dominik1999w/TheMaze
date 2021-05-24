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
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import java.util.UUID;

import connection.ClientFactory;
import connection.game.GameClient;
import connection.map.MapClient;
import connection.state.StateClient;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import renderable.MapView;
import types.SkinType;
import util.Point2D;

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

    private final UUID playerID;
    private final GameApp game;
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Stage stage;
    private final AsyncExecutor asyncExecutor;
    private AsyncResult<Void> task;
    private GameScreen gameScreen;

    /* UI Containers */
    private Container<Table> menuContainer;
    private Container<Actor> sliderContainer;
    private Label status;
    private TextButton startGame;
    private MapView mapView;
    private OrthographicCamera camera;

    /* Clients */
    private MapClient mapClient;
    private GameClient gameClient;
    private StateClient stateClient;

    /* UI specifications */
    private final float initialZoom = 0.166f;
    private final int defaultHeight = 1080;
    private final int minMapLength = 5;
    private final int maxMapLength = 50;
    private final int defaultSeed = 0;
    private final Random random = new Random();
    private boolean startGameValue;

    public MenuScreen(UUID playerID, GameApp game, SpriteBatch batch, AssetManager assetManager) {
        this.playerID = playerID;
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
            stateClient = ClientFactory.newStateClient(HOST, PORT);

            mapClient.connect(playerID);

            Gdx.app.postRunnable(() -> status.setText("server status: connected"));
            return null;
        });
    }

    private void buildUI() {
        buildMenuContainer();
        buildMap();
        buildSliderContainer();

        status = new Label("server status: connecting", skin, "big");
        status.setPosition(10, 0);

        stage.addActor(status);
        stage.addActor(menuContainer);
        stage.addActor(sliderContainer);

        Gdx.input.setInputProcessor(stage);
    }

    int prevLength = minMapLength;
    int prevSeed = defaultSeed;
    boolean sliderShown = false;
    Point2D initialPosition = new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE);

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        syncState();

        batch.begin();
        mapView.render(batch);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void syncState() {
        if (task.isDone()) {
            mapClient.syncState(prevLength, prevSeed, startGameValue);
            mapClient.dispatchMessages(new MapClient.ServerResponseHandler() {
                @Override
                public void displayAdminUI() {
                    if (!sliderShown) {
                        sliderContainer.setVisible(true);
                        startGame.setVisible(true);
                        sliderShown = true;
                    }
                }

                @Override
                public void updateMap(int mapLength, int seed) {
                    if (prevLength != mapLength || prevSeed != seed) {
                        calculateMap(mapLength, seed);
                    }
                }

                @Override
                public void updateInitialPosition(Point2D position) {
                    initialPosition = position;
                }

                @Override
                public void startGame(int mapLength, int seed, boolean isHost) {
                    MapGenerator mapGenerator = new MapGenerator(mapLength);
                    Map map = mapGenerator.generateMap(seed);
                    gameScreen = new GameScreen(playerID, batch, gameClient, stateClient, initialPosition, map, assetManager);
                    game.setScreen(gameScreen);
                }
            });
        }
    }

    @Override
    public void dispose() {
        if (task.isDone()) {
            mapClient.disconnect();
            gameClient.disconnect();
        }
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

        startGame = new TextButton("Start Game", skin);
        startGame.getLabel().setFontScale(optionFontScale * Gdx.graphics.getHeight() / defaultHeight);
        startGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGameValue = true;
                startGame.setTouchable(Touchable.disabled);
            }
        });
        startGame.setVisible(false);
        info.add(startGame).fillX();

        info.row();

        TextButton quitGame = new TextButton("Quit", skin);
        quitGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
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
                calculateMap((int) slider.getValue(), random.nextInt());
            }
        });
        config.add(slider).growX();

        sliderContainer.setActor(config);
    }

    private void buildMap() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapView = new MapView(new MapGenerator(minMapLength).generateMap(defaultSeed), assetManager);
        calculateMap(minMapLength, defaultSeed);
    }

    private void calculateMap(int length, int seed) {
        prevLength = length;
        prevSeed = seed;

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
}
