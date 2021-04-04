package game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connection.GrpcClient;
import loader.GameLoader;
import map.Map;

public class OnlineGameScreen extends OfflineGameScreen {

    private final GrpcClient client;
    private int frameCounter = 0;

    private static final String HOST =
            //"10.0.2.2"
            //"localhost"
            "10.232.0.13"
    ;

    private static final int PORT =
            50051
            //8080
    ;

    public OnlineGameScreen(SpriteBatch batch, GameLoader loader, Map map) {
        super(batch, loader, map);
        this.client = new GrpcClient(this.playerView, HOST, PORT);
    }

    @Override
    public void show() {
        this.gameUI.setDebugText(""+client.connect());
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (frameCounter % 5 == 0) client.syncGameState();
        frameCounter++;
    }
}
