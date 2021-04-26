package connection;

public interface MapClient extends Client {
    boolean isHost();
    int getMapLength();
    void setMapLength(int value);
    int getSeed();
    void setSeed(int seed);
}
