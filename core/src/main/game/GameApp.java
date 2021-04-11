package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLoggerFactory;
import io.grpc.netty.shaded.io.netty.util.internal.logging.JdkLoggerFactory;

public class GameApp extends Game {
    private SpriteBatch batch;
    private ScreenAdapter gameScreen;

    @Override
    public void create() {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
//        Database db = new Database(new FileEngine());
//        db.saveMap(map);
        batch = new SpriteBatch();
        gameScreen = new OnlineGameScreen(batch);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameScreen.dispose();
    }
}