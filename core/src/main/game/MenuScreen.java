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

import connection.GameClient;
import connection.GameClientFactory;
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


    private GameScreen gameScreen;
    private GameClient client;
    private final AsyncExecutor asyncExecutor;
    private AsyncResult<Void> task;
    private MapView mapView;
    private OrthographicCamera camera;
    private Label status;

    public MenuScreen(GameApp game, SpriteBatch batch, AssetManager assetManager) {
        this.game = game;
        this.batch = batch;
        this.assetManager = assetManager;
        this.skin = assetManager.get(SkinType.GLASSY.getName());
        this.asyncExecutor = new AsyncExecutor(1);
        this.stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        connect();
        buildUI();
    }

    void connect() {
        task = asyncExecutor.submit(() -> {
            client = new GameClientFactory(HOST, PORT).getClient();
            return null;
        });
    }

    void buildUI() {
        int defaultHeight = 1080;

        Container<Table> leftContainer = buildInfo(defaultHeight);
        Container<Table> rightContainer = buildConfig(leftContainer, defaultHeight);

        status = new Label("server status: connecting", skin, "big");
        status.setPosition(10, 0);

        stage.addActor(status);
        stage.addActor(leftContainer);
        stage.addActor(rightContainer);

        Gdx.input.setInputProcessor(stage);
    }

    private Container<Table> buildInfo(int defaultHeight) {
        float leftContainerWidth = Gdx.graphics.getWidth() * 0.45f;
        float leftContainerHeight = Gdx.graphics.getHeight() * 0.95f;

        Container<Table> leftContainer = new Container<>();
        leftContainer.setSize(leftContainerWidth, leftContainerHeight);
        leftContainer.setPosition(0, (Gdx.graphics.getHeight() - leftContainerHeight) / 2);
        leftContainer.fillX();
        leftContainer.setDebug(true);

        Table info = new Table();
        info.setFillParent(true);
        info.defaults().pad(10.0f);

        Label title = new Label("The Maze", skin, "big");
        title.setFontScale(2.5f * Gdx.graphics.getHeight() / defaultHeight);
        info.add(title);

        info.row().padTop(50.0f);

        TextButton startGame = new TextButton("Start Game", skin);
        startGame.getLabel().setFontScale(1.5f * Gdx.graphics.getHeight() / defaultHeight);
        startGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (task.isDone()) { // TODO: check for server's connection
                    gameScreen = new GameScreen(batch, client, assetManager);
                    game.setScreen(gameScreen);
                }
            }
        });
        info.add(startGame).fillX();

        info.row();

        TextButton quitGame = new TextButton("Quit", skin);
        quitGame.getLabel().setFontScale(1.5f * Gdx.graphics.getHeight() / defaultHeight);
        info.add(quitGame).fillX();

        leftContainer.setActor(info);
        return leftContainer;
    }

    private Container<Table> buildConfig(Container<Table> leftContainer, int defaultHeight) {
        float initialZoom = 0.166f;

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = initialZoom * defaultHeight / Gdx.graphics.getHeight();
        camera.position.set(
                camera.zoom * (0.5f * Gdx.graphics.getWidth() - leftContainer.getWidth()) + (float) MapConfig.WALL_THICKNESS / 2,
                camera.zoom * leftContainer.getHeight() / 2,
                0);
        camera.update();

        final Slider slider = new Slider(5, 50, 1, false, skin);

        float rightContainerWidth = ((MapConfig.BOX_SIZE) * slider.getMinValue()) / camera.zoom;
        float rightContainerHeight = leftContainer.getHeight();

        Container<Table> rightContainer = new Container<>();
        rightContainer.setSize(rightContainerWidth, rightContainerHeight);
        rightContainer.setPosition(leftContainer.getWidth(), (Gdx.graphics.getHeight() - rightContainerHeight) / 2);
        rightContainer.fillX();
        rightContainer.setDebug(true);

        Table config = new Table();
        config.setFillParent(true);

        MapGenerator mapGenerator = new MapGenerator((int) slider.getMinValue());
        Map map = mapGenerator.generateMap(new Random().nextInt());
        mapView = new MapView(map, assetManager);
        mapView.setView(camera);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (!slider.isDragging()) {
                    return;
                }

                int value = (int) slider.getValue();
                MapGenerator mapGenerator1 = new MapGenerator(value);
                mapView.setMap(mapGenerator1.generateMap(new Random().nextInt()));
                camera.zoom = initialZoom * defaultHeight / Gdx.graphics.getHeight() * value / slider.getMinValue();
                camera.position.set(
                        camera.zoom * (0.5f * Gdx.graphics.getWidth() - leftContainer.getWidth()) + (float) MapConfig.WALL_THICKNESS / 2,
                        camera.zoom * leftContainer.getHeight() / 2,
                        0);
                camera.update();
                mapView.setView(camera);
            }
        });

        config.add(slider).growX();

        rightContainer.setActor(config);
        return rightContainer;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (task.isDone()) {
            status.setText("server status: connected");
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
