package connection;

import world.World;

public class NoOpGameClient implements GameClient {
    @Override
    public void connect() {
    }

    @Override
    public void syncState() {

    }

    @Override
    public void enterGame(World world) {

    }

    @Override
    public void notifyInput(float x, float y, boolean shootPressed) {

    }
}
