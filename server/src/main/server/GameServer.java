package server;

public interface GameServer {

    void start() throws Exception;
    void blockUntilShutdown() throws Exception;

}
