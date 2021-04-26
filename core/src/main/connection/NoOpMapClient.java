package connection;

public class NoOpMapClient implements MapClient {
    @Override
    public boolean isHost() {
        return false;
    }

    @Override
    public int getMapLength() {
        return 5;
    }

    @Override
    public void setMapLength(int value) {

    }

    @Override
    public int getSeed() {
        return 0;
    }

    @Override
    public void setSeed(int seed) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void syncState() {

    }
}
