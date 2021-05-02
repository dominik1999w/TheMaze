package server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import service.GameService;
import service.MapService;
import service.PlayerIDInterceptor;

public class GrpcServer implements GameServer {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    private final Server server;

    public GrpcServer(int port, GameService gameService, MapService mapService) {
        server = ServerBuilder.forPort(port)
                .addService(gameService)
                .addService(mapService)
                .addService(ServerInterceptors.intercept(gameService, new PlayerIDInterceptor()))
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + server.getPort());
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
