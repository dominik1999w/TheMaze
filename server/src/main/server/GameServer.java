package server;

public interface GameServer {

    void start(int port) throws Exception;
    void blockUntilShutdown() throws Exception;

}
