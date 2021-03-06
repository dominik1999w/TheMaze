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
import java.util.Properties;
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
import util.ServerConfig;

public class MenuScreen extends ScreenAdapter {
    private final UUID playerID;
    private String name;
    private java.util.Map<String, String> clientsNames;

    private final GameApp game;
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Stage stage;
    private final AsyncExecutor asyncExecutor;
    private AsyncResult<String> connectionResult;
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
    private final int defaultGeneratorType = 1;
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
        connectionResult = asyncExecutor.submit(() -> {
            Properties serverProperties = new Properties();
            serverProperties.load(Gdx.files.internal("server.properties").read());

            String host = serverProperties.getProperty("host");
            int portMain = Integer.parseInt(serverProperties.getProperty("port-main"));
            int portVoice = Integer.parseInt(serverProperties.getProperty("port-voice"));
            int serverTickRate = Integer.parseInt(serverProperties.getProperty("tick-rate"));

            ServerConfig.SERVER_UPDATE_RATE = serverTickRate;

            gameClient = ClientFactory.newGameClient(host, portMain);
            mapClient = ClientFactory.newMapClient(host, portMain);
            stateClient = ClientFactory.newStateClient(host, portMain);
            voiceClient = ClientFactory.newVoiceClient(host, portVoice);

            mapClient.connect(playerID);
            voiceClient.connect(playerID);

            return mapClient.getUserName();
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
    int prevGeneratorType = defaultGeneratorType;
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
        if (connectionResult.isDone()) {
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
        if (connectionResult.isDone()) {
            if (name == null) {
                name = connectionResult.get();
                username.setText("name: " + name);
            }

            mapClient.syncState(prevLength, prevSeed, prevGeneratorType, startGameValue);
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
                public void updateMap(int mapLength, int seed, int generatorType) {
                    if (prevLength != mapLength || prevSeed != seed || prevGeneratorType != generatorType) {
                        calculateMap(mapLength, seed, generatorType);
                    }
                }

                @Override
                public void updateInitialPosition(Point2D position) {
                    initialPosition = position;
                }

                @Override
                public void updateClientsNames(java.util.Map<String, String> names) {
                    MenuScreen.this.clientsNames = names;
                }

                @Override
                public void startGame(int mapLength, int seed, int generatorType, boolean isHost) {
                    MapGenerator mapGenerator = new MapGenerator(mapLength);
                    Map map = mapGenerator.generateMap(seed, generatorType);
                    gameScreen = new GameScreen(
                            playerID, clientsNames, batch, game, gameClient, stateClient, voiceClient, initialPosition, map, assetManager
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
        slider.setWidth(400);

        TextButton mapType1 = Menu.getTextButton("random", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                calculateMap((int) slider.getValue(), random.nextInt(), 1);
            }
        });

        TextButton mapType2 = Menu.getTextButton("caves", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                calculateMap((int) slider.getValue(), random.nextInt(), 2);
            }
        });

        TextButton mapType3 = Menu.getTextButton("dungeon", skin, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                calculateMap((int) slider.getValue(), random.nextInt(), 3);
            }
        });


        sliderContainer = new Menu.Builder()
                //.defaultSettings(width, height, menuContainer.getWidth(), (Gdx.graphics.getHeight() - height) / 2)
                .defaultSettings(menuContainer.getWidth() * 0.65f, height, menuContainer.getX() + 0.175f * menuContainer.getWidth(), -(float) Gdx.graphics.getHeight() / 2)
                .addTable()
                .addSlider(slider, new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        if (!slider.isDragging()) {
                            return;
                        }
                        calculateMap((int) slider.getValue(), random.nextInt(), prevGeneratorType);
                    }
                })
                .addPadding(10.0f)
                .addTextButton(mapType1, 0.8f)
                .addPadding(3.0f)
                .addTextButton(mapType2, 0.8f)
                .addPadding(3.0f)
                .addTextButton(mapType3, 0.8f)
                .build();

        sliderContainer.setVisible(false);
    }

    private void buildMap() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapView = new MapView(new MapGenerator(minMapLength).generateMap(defaultSeed, defaultGeneratorType), assetManager);
        calculateMap(minMapLength, defaultSeed, defaultGeneratorType);
    }

    private void calculateMap(int length, int seed, int generatorType) {
        prevLength = length;
        prevSeed = seed;
        prevGeneratorType = generatorType;

        updateCamera(length);

        mapView.setMap(new MapGenerator(length).generateMap(seed, generatorType));
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
