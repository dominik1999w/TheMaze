package server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import service.GameService;

public class GrpcServer implements GameServer {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    private Server server;

    public void start(int port) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new GameService(logger))
                .build().start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down");
            try {
                GrpcServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
