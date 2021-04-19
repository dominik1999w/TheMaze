package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import connection.GameClientFactory;
import types.SkinType;

public class MenuScreen extends ScreenAdapter {

    private final GameApp game;
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Stage stage;
    private final Table table;

    private static final String HOST =
            //"10.0.2.2"
            "localhost"
//            "10.232.0.13"
            ;

    private static final int PORT =
            50051
            //8080
            ;

    private GameScreen gameScreen;

    public MenuScreen(GameApp game, SpriteBatch batch, AssetManager assetManager) {
        this.game = game;
        this.batch = batch;
        this.assetManager = assetManager;
        this.skin = assetManager.get(SkinType.GLASSY.getName());
        this.table = new Table();
        this.stage = new Stage();
        build();
    }

    void build() {
        table.setFillParent(true);
        stage.addActor(table);
        table.setDebug(true);

        TextButton newGame = new TextButton("New Game", skin);
        newGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                gameScreen = new GameScreen(batch, new GameClientFactory(HOST, PORT).getClient(), assetManager);
                game.setScreen(gameScreen);
            }
        });


        table.add(newGame).fillX().uniformX();
        Gdx.input.setInputProcessor(stage);
        stage.draw();
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        stage.dispose();
    }
}
