package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private String host;
    private int port;
    private long lastHeartbeat;
    
    // Identifier could be host:port
    public String getId() {
        return host + ":" + port;
    }
}
