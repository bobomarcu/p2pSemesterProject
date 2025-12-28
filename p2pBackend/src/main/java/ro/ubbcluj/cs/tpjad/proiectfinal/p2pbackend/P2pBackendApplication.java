package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P2pBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(P2pBackendApplication.class, args);
    }

}
