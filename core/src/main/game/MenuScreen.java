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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import connection.ClientFactory;
import connection.game.GameClient;
import connection.map.MapClient;
import connection.state.StateClient;
import connection.voice.VoiceClient;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import renderable.MapView;
import types.SkinType;
import util.Menu;
import util.Point2D;

public class MenuScreen extends ScreenAdapter {
    private static final String HOST =
//            "10.0.2.2"
//            "localhost"
            "10.232.0.13"
//            "192.168.1.15"
//            "54.177.126.239"
            ;

    private static final int PORT = 50051;

    private final UUID playerID;
    private String name;

    private final GameApp game;
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Stage stage;
    private final AsyncExecutor asyncExecutor;
    private AsyncResult<Void> task;
    private GameScreen gameScreen;

    /* UI Containers */
    private Container<Actor> menuContainer;
    private Container<Actor> sliderContainer;

    private TextButton startButton;
    private MapView mapView;
    private OrthographicCamera camera;
    private Label username;

    /* Clients */
    private MapClient mapClient;
    private GameClient gameClient;
    private StateClient stateClient;
    private VoiceClient voiceClient;

    /* UI specifications */
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
            voiceClient = ClientFactory.newVoiceClient(HOST, PORT+1);

            mapClient.connect(playerID);
            voiceClient.connect(playerID);

            this.name = mapClient.getUserName();

            Gdx.app.postRunnable(() -> username.setText("name: " + name));
            return null;
        });
    }

    private void buildUI() {
        menuContainer = Menu.buildMenuContainer(getTextButtons(), skin);
        buildMap();
        buildSliderContainer();

        username = new Label("name:", skin, "big");
        username.setPosition(10, 0);

        stage.addActor(username);
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

    @Override
    public void dispose() {
        if (task.isDone()) {
            mapClient.disconnect();
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

    private void syncState() {
        if (task.isDone()) {
            mapClient.syncState(prevLength, prevSeed, startGameValue);
            mapClient.dispatchMessages(new MapClient.ServerResponseHandler() {
                @Override
                public void displayAdminUI() {
                    if (!sliderShown) {
                        sliderContainer.setVisible(true);
                        startButton.setVisible(true);
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
                    gameScreen = new GameScreen(
                            playerID, name, batch, game, gameClient, stateClient, voiceClient, initialPosition, map, assetManager
                    );
                    game.setScreen(gameScreen);
                }
            });
        }
    }

    private List<TextButton> getTextButtons() {
        List<TextButton> buttons = new ArrayList<>();
        startButton = Menu.getTextButton("Start Game", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGameValue = true;
            }
        });
        buttons.add(startButton);
        startButton.setVisible(false);
        TextButton quitButton = Menu.getTextButton("Quit", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        buttons.add(quitButton);
        return buttons;
    }

    private void buildSliderContainer() {
        float width = (MapConfig.BOX_SIZE) * minMapLength / camera.zoom;
        float height = menuContainer.getHeight();

        final Slider slider = new Slider(minMapLength, maxMapLength, 1, false, skin);

        TextButton mapType1 = Menu.getTextButton("random", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MapGenerator.chooseGenerator(1);
                calculateMap((int) slider.getValue(), random.nextInt());
            }
        });

        TextButton mapType2 = Menu.getTextButton("cheese", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MapGenerator.chooseGenerator(2);
                calculateMap((int) slider.getValue(), random.nextInt());
            }
        });

        TextButton mapType3 = Menu.getTextButton("dungeon", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MapGenerator.chooseGenerator(3);
                calculateMap((int) slider.getValue(), random.nextInt());
            }
        });


        sliderContainer = new Menu.Builder()
                //.defaultSettings(width, height, menuContainer.getWidth(), (Gdx.graphics.getHeight() - height) / 2)
                .defaultSettings(width, height,0,-(float)Gdx.graphics.getHeight()/2)
                .addTable()
                .addSlider(slider, new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        if (!slider.isDragging()) {
                            return;
                        }
                        calculateMap((int) slider.getValue(), random.nextInt());
                    }
                })
                .addPadding(10.0f)
                //.addDefaultPadding(10.0f)
                .addTextButton(mapType1,0.8f)
                .addTextButton(mapType2,0.8f)
                .addTextButton(mapType3,0.8f)
                .build();

        sliderContainer.setVisible(false);
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
        float initialZoom = 0.166f;
        camera.zoom = initialZoom * Menu.defaultHeight / Gdx.graphics.getHeight() * mapLength / minMapLength;
        camera.position.set(
                camera.zoom * (0.5f * Gdx.graphics.getWidth() - menuContainer.getWidth()) + 0.5f * MapConfig.WALL_THICKNESS,
                camera.zoom * menuContainer.getHeight() * 0.5f,
                0);
        camera.update();
    }
}
