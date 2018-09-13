package fetcher.rpc;

import fetcher.services.AnalyticsRPCServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class RPCServer implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private AnalyticsRPCServiceImpl analyticsRPCServiceImpl;

    @Value("${grpc.server.port}")
    private int port;

    private Server server;

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(analyticsRPCServiceImpl)
                .build().start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                RPCServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        try {
            log.info("start rpc server");
            start();
            blockUntilShutdown();
        } catch (Exception e) {
            log.error("fail to start grpc server");
        }
    }
}
