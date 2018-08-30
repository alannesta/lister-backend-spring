package fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import fetcher.rpc.RPCServer;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class Application {
    public static void main(String args[]) {
        log.info("start springboot tomcat");
        SpringApplication.run(Application.class, args);

        // try {
        //     log.info("start rpc server");
        //     RPCServer rpcServer = new RPCServer();
        //     rpcServer.start();
        //     rpcServer.blockUntilShutdown();
        // } catch (Exception e) {
        //     log.error("fail to start grpc server");
        // }
    }
}
