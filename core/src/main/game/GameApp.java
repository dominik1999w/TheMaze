package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connection.GameClientFactory;
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
        gameScreen = new GameScreen(batch, new GameClientFactory(HOST, PORT).getClient());
        setScreen(gameScreen);
    }

    private static final String HOST =
            //"10.0.2.2"
            //"localhost"
            "10.232.0.13"
            ;

    private static final int PORT =
            50051
            //8080
            ;

    @Override
    public void dispose() {
        batch.dispose();
        gameScreen.dispose();
    }
}