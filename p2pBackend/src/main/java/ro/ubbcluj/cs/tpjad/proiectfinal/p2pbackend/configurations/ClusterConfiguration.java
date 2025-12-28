package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Getter
public class ClusterConfiguration{
    @Value("${p2p.server.bootstrap}")
    private String bootstrapServer;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${p2p.server.hostname:localhost}")
    private String hostname;
}
