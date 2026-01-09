package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.configurations.ClusterConfiguration;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.GossipRequest;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ClusterService {

    private final ClusterConfiguration clusterConfiguration;
    private final Map<String, Node> peers = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    public ClusterService(ClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
    }

    @Scheduled(fixedRate = 5000)
    public void gossip() {
        if (peers.isEmpty()) {
            // Try to join via bootstrap if no peers and bootstrap is set
            joinViaBootstrap();
            return;
        }

        // Pick a random peer
        List<Node> peerList = new ArrayList<>(peers.values());
        Node target = peerList.get(random.nextInt(peerList.size()));

        try {
            log.info("Gossiping with {}", target.getId());
            // TODO: Extend GossipRequest to include file manifests/data for synchronization
            GossipRequest request = new GossipRequest(getSelfNode(), peerList);
            String url = "http://" + target.getHost() + ":" + target.getPort() + "/cluster/gossip";
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            log.error("Failed to gossip with {}: {}", target.getId(), e.getMessage());
            // TODO: Implement failure detection - remove peer if it fails repeatedly or hasn't updated heartbeat
        }
    }

    public void handleGossip(GossipRequest request) {
        log.info("Received gossip from {}", request.getSender().getId());
        mergePeers(request.getPeers());
        mergePeers(List.of(request.getSender()));
        // TODO: Handle incoming file manifests/data updates - check for new files or updates and download them
    }

    private void mergePeers(List<Node> newPeers) {
        if (newPeers == null) return;
        for (Node node : newPeers) {
            if (node.getId().equals(getSelfNode().getId())) {
                continue; // Skip self
            }
            peers.merge(node.getId(), node, (existing, replacement) -> {
                if (replacement.getLastHeartbeat() > existing.getLastHeartbeat()) {
                    return replacement;
                }
                return existing;
            });
        }
    }

    private void joinViaBootstrap() {
        String bootstrap = clusterConfiguration.getBootstrapServer();
        if (bootstrap != null && !bootstrap.isEmpty()) {
            // Avoid bootstrapping with self if I am the bootstrap node
            String selfId = getSelfNode().getId();
            if (bootstrap.equals(selfId)) {
                return; 
            }
            
            String[] parts = bootstrap.split(":");
            if (parts.length == 2) {
                try {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    // Just send a gossip to bootstrap to register
                    Node bootstrapNode = new Node(host, port, System.currentTimeMillis());
                    GossipRequest request = new GossipRequest(getSelfNode(), new ArrayList<>());
                    String url = "http://" + host + ":" + port + "/cluster/gossip";
                    restTemplate.postForEntity(url, request, Void.class);
                    log.info("Sent join request to bootstrap {}", bootstrap);
                    
                    // Add bootstrap as known peer
                    peers.put(bootstrapNode.getId(), bootstrapNode);
                } catch (Exception e) {
                    log.error("Failed to join via bootstrap {}: {}", bootstrap, e.getMessage());
                }
            }
        }
    }

    private Node getSelfNode() {
        return new Node(clusterConfiguration.getHostname(), clusterConfiguration.getServerPort(), System.currentTimeMillis());
    }
    
    public List<Node> getPeers() {
        return new ArrayList<>(peers.values());
    }
}
