package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.GossipRequest;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services.ClusterService;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.status(HttpStatus.OK).body("Cluster OK. Known peers: " + clusterService.getPeers().size());
    }

    @PostMapping("/gossip")
    public ResponseEntity<?> receiveGossip(@RequestBody GossipRequest request) {
        // TODO: Secure this endpoint (e.g., validate JWT token, checking for trusted peer role)
        clusterService.handleGossip(request);
        return ResponseEntity.ok().build();
    }

}
