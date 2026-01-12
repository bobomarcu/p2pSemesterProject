package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GossipRequest {
    private Node sender;
    private List<Node> peers;
}
